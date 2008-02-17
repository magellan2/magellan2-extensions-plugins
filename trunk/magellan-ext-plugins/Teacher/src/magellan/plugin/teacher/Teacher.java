// class magellan.plugin.ECTester
// created on Dec 30, 2007
//
// Copyright 2003-2007 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc., 
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
package magellan.plugin.teacher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import magellan.library.GameData;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

public class Teacher {
  
  Unit unit;
  GameData world;
  UnitContainer container;
  
  private UserInterface ui = new NullUserInterface();

  Teacher(UnitContainer container, UserInterface ui) {
    this.container = container;
    this.ui = ui;
  }

  class TeachingHelper {
    private final Logger log = Logger.getInstance(TeachingHelper.class);

    private SUnit[] units;
    java.lang.String delim = " ";

    Random random = new Random();

    public class SUnit {

      Unit unit;
      Map<String, Integer> teach = new HashMap<String, Integer>();
      Map<String, Double> learn = new HashMap<String, Double>();
      private int index;
      private ArrayList<Integer> teachers = new ArrayList<Integer>();

      SUnit(Unit unit) {
        this.unit = unit;
      }

      public void addTeach(String talent, Integer prio) {
        teach.put(talent, prio);
      }

      public void addLearn(String talent, Double prio) {
        learn.put(talent, prio);
      }

      public Unit getUnit() {
        return unit;
      }

      public double getPrio(String learning) {
        if (learning == null)
          return 0;
        return (Double) (learn.get(learning) != null ? learn.get(learning) : -1);
      }

      public int getMaximumDifference(String teaching) {
        if (teaching == null)
          return 1;
        return (teach.get(teaching) != null ? teach.get(teaching) : 1);
      }

      public Set<String> getTalents() {
        return learn.keySet();
      }

      void setIndex(int index) {
        this.index = index;
      }

      public int getIndex() {
        return index;
      }

      void addTeacher(int index) {
        teachers.add(index);
      }

      public int getRandomTeacher() {
        if (teachers.size()==0)
          return -1;
        return teachers.get(random.nextInt(teachers.size()));
      }

      public String toString(){
        return index + ":" + getUnit().toString();
      }
    }

    class Solution implements Comparable {

      class Info {
        private SUnit unit = null;
        String learning = null;
        private int students = 0;
        private int teacher = -1;

        public Info(SUnit unit) {
          this.unit = unit;
          assignLearn();
        }

        public void assignLearn() {
          learning = (String) unit.getTalents().toArray()[random.nextInt(unit.getTalents().size())];
        }

        public String toString() {
          return unit.toString() + " " + learning + " " + students + " " + teacher;
        }

        public SUnit getSUnit() {
          return unit;
        }

        public Unit getUnit() {
          return unit.getUnit();
        }

        public void clearTeachers() {
          teacher = -1;
        }

        public void addTeacher(int t) {
          teacher = t;
        }

        public void removeTeacher(int t) {
          if (teacher == t)
            teacher = -1;
        }

        public int getNumTeachers() {
          if (teacher == -1)
            return 0;
          return infos[teacher].getUnit().getModifiedPersons();
        }

        public Collection<Integer> getTeacherSet() {
          if (teacher == -1)
            return Collections.emptySet();
          else
            return Collections.singleton(teacher);
        }

        public int getTeacher() {
          return teacher;
        }

      }

      SUnit[] units;
      Info[] infos;

      boolean changed = true;
      double result = 0;

      public Solution(SUnit[] units) {
        this.units = units;
        init();
      }

      void init() {
        this.infos = new Info[this.units.length];
        for (int i = 0; i < infos.length; ++i) {
          Info info = new Info(units[i]);
          infos[i] = info;
        }
        changed = true;
        result = 0;
      }

      public double evaluate() {
        if (!changed)
          return result;
        result = 0;
        for (int i = 0; i < infos.length; ++i) {
          Info info = infos[i];
          SUnit su = info.getSUnit();
          double value = 0;
          if (info.learning != null) {
            value = su.getPrio(info.learning);
            value *= su.getUnit().getModifiedPersons();
            value *= getLevel(su.getUnit(), info.learning);
            if (info.getNumTeachers() > 0) {
              Info teacher = infos[info.teacher];
              value *= 2;
              int sLevel = getLevel(info.getUnit(), info.learning);
              int tLevel = getLevel(teacher.getUnit(), info.learning);
              int maxDiff = teacher.getSUnit().getMaximumDifference(info.learning);
              if (maxDiff == 1) {
                value = 0;
              } else if (tLevel - sLevel < 2) {
                log.warn("diff<2");
                value = 0;
              } else if (maxDiff == 0 || maxDiff > tLevel - sLevel) {
                value *= (1 + sLevel / (double) tLevel);
              } else
                value *= 2;
            }
          }
          result += value;
        }
        return result;
      }

      public int compareTo(Object o) {
        if (o instanceof Solution) {
          double e2 = ((Solution) o).evaluate();
          double e1 = evaluate();

          return (int) (e2 > e1 ? Math.ceil(e2 - e1) : Math.floor(e2 - e1));
        } else
          throw new IllegalArgumentException("wrong type for comparison");
      }

      public void mutate(double d) {
        double mutationProb = d;
        double teachingProb = .3;
        double teacherProb = .3;

        for (int i = 0; i < infos.length; ++i) {
          Info info = infos[i];
          SUnit su = info.getSUnit();
          if (random.nextDouble() < mutationProb) {

            // change teacher status
            if (random.nextDouble() < teachingProb) {
              if (info.learning == null) {
                // learn new talent
                info.assignLearn();
                info.clearTeachers();
                info.students = 0;
              } else {
                // become teacher
                info.learning = null;
                info.students = 0;
                if (info.getTeacher() != -1)
                  infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
                info.clearTeachers();
              }
            }

            // assign new teacher
            if (info.learning != null && random.nextDouble() < teacherProb) {
              if (info.getTeacher() != -1)
                infos[info.getTeacher()].students -= info.getUnit().getModifiedPersons();
              info.clearTeachers();
            }
          }
        }

        assignTeachers();
        changed = true;
      }

      protected void assignTeachers() {
        Collection<Integer> teacherSet = new ArrayList<Integer>();

        // reset teachers
        for (int i = 0; i < infos.length; ++i) {
          Info info = infos[i];
          if (info.learning == null) {
            teacherSet.add(new Integer(i));
          }
          if (info.getTeacher() != -1 && infos[info.getTeacher()].learning != null)
            info.clearTeachers();

        }

        if (teacherSet.size() <= 0)
          return;

        // Integer[] teachers = (Integer[]) teacherSet.toArray(new Integer[0]);

        // assign teachers
        for (int i = 0; i < infos.length; ++i) {
          Info student = infos[i];
          if (student.learning != null) {
            if (student.getTeacher() == -1) {
              // find new teacher
              int teacher = student.getSUnit().getRandomTeacher();
              if (teacher!=-1 && validTeacher(student, teacher)) {
                assignTeacher(student, teacher);
              }
              // int firstTeacher = random.nextInt(teachers.length);
              // for (int j = 0; j < teachers.length; ++j) {
              // int current = (firstTeacher + j) % teachers.length;
              // // suitable teacher?
              // if (validTeacher(student, teachers[current])) {
              // assignTeacher(student, teachers[current]);
              // break;
              // }
              // }
            }
          }
        }
        // // assign teachers, 2nd try
        // for (int i = 0; i < infos.length; ++i) {
        // Info student = infos[i];
        // if (student.learning != null) {
        // if (student.getTeacher()==-1) {
        // // find new teacher
        // int firstTeacher = random.nextInt(teachers.length);
        // for (int j = 0; j < teachers.length; ++j) {
        // int current = (firstTeacher + j) % teachers.length;
        // // suitable teacher?
        // if (validTeacher(student, teachers[current], true)) {
        // assignTeacher(student, teachers[current]);
        // break;
        // }
        // }
        // }
        // }
        // }

      }

      public void mate(Solution solution, Solution solution2) {
        if (solution.units != solution2.units)
          throw new IllegalArgumentException("incompatible solutions");
        init();

        byte[] parents = new byte[infos.length];
        random.nextBytes(parents);

        // first step: only assign "roles"
        for (int i = 0; i < infos.length; ++i) {
          Info info = infos[i];
          Info parent;
          if (parents[i] % 2 == 0)
            parent = solution.infos[i];
          else
            parent = solution2.infos[i];
          info.learning = parent.learning;
          info.students = 0;
          info.clearTeachers();
        }

        // assign teachers
        for (int i = 0; i < infos.length; ++i) {
          Info student = infos[i];
          Info parent;
          if (parents[i] % 2 == 0)
            parent = solution.infos[i];
          else
            parent = solution2.infos[i];
          if (student.learning != null && parent.getTeacher() != -1) {
            if (validTeacher(student, parent.getTeacher()))
              assignTeacher(student, parent.getTeacher());
          }
        }
        assignTeachers();
        changed = true;
      }

      private boolean validTeacher(Info student, int t) {
        return validTeacher(student, t, false);
      }

      private boolean validTeacher(Info student, int t, boolean partial) {
        Info teacher = infos[t];

        if (teacher.learning != null
            || getLevel(teacher.getUnit(), student.learning) < getLevel(student.getUnit(),
                student.learning) + 2)
          return false;
        else
          return partial
              || teacher.getUnit().getModifiedPersons() * 10 >= teacher.students
                  + student.getUnit().getModifiedPersons();

      }

      public void assignTeacher(Info student, int t) {
        Info teacher = infos[t];
        student.addTeacher(t);
        teacher.students += student.getUnit().getModifiedPersons();
      }
    }

    /**
     * Returns the skill level of a unit. For example
     * getLevel(unit,"Unterhaltung")
     */
    public int getLevel(Unit unit, String skillName) {
      Collection<Skill> skills = unit.getModifiedSkills();
      if (skills != null) {
        for (Skill skill : skills) {
          if (skill.getSkillType().getName().equalsIgnoreCase(skillName)) {
            return skill.getLevel();
          }
        }
      }
      return 0;
    }

    public Collection<SUnit> getUnits() {
      Collection<SUnit> result = new ArrayList<SUnit>(container.units().size());
      for (Unit u : container.units()) {
        SUnit su = parseUnit(u);
        if (su != null) {
          result.add(su);
          su.setIndex(result.size() - 1);
        }
      }
      for (SUnit student : result) {
        for (SUnit teacher : result) {
          if (valid(student, teacher))
            student.addTeacher(teacher.getIndex());
        }
      }
      return result;
    }

    private boolean valid(SUnit student, SUnit teacher) {
      boolean result = false;
      for (String talent : student.getTalents()) {
        int diff = teacher.getMaximumDifference(talent);
        if (diff==3)
          diff = 3;
        if (student.getPrio(talent) > 0
            && diff != 1
            && getLevel(teacher.getUnit(), talent) - getLevel(student.getUnit(), talent) <= Math
                .max(2, diff)) {
          result = true;
          break;
        }
      }
      return result;
    }

    public SUnit parseUnit(Unit u) {
      SUnit su = null;
      boolean errorFlag = false;
      for (java.lang.String o : u.getOrders()) {
        int start = o.indexOf("$T");
        if (start != -1) {
          try {
            java.util.StringTokenizer st =
                new java.util.StringTokenizer(o.substring(start + 2), delim, false);
            while (st.hasMoreElements()) {
              String talent = st.nextToken();
              int prio = Integer.parseInt(st.nextToken());
              if (su == null)
                su = new SUnit(u);
              if (talent.equals("ALLES")){
                if (u.getModifiedSkills().isEmpty())
                  continue;
                
                // add all skills weighted by their level
                for (Skill s : u.getModifiedSkills()){
                  su.addTeach(s.getName(), prio);
                }
              } else{
                su.addTeach(talent, prio);
              }
            }
          } catch (Exception e) {
            log.warn(e+"parse error, unit " + u + " line " + o);
            errorFlag = true;
          }
        }

        start = o.indexOf("$L");
        if (start != -1) {
          try {
            java.util.StringTokenizer st =
                new java.util.StringTokenizer(o.substring(start + 2), delim, false);
            while (st.hasMoreElements()) {
              String talent = st.nextToken();
              double prio = Double.parseDouble(st.nextToken()), prio2 = 0;
              if (su == null)
                su = new SUnit(u);
              if (talent.equals("ALLES")){
                prio2 = Integer.parseInt(st.nextToken());

                if (u.getModifiedSkills().isEmpty())
                  continue;
                
                // find min an max skill
                int maxSkill = 1, minSkill = Integer.MAX_VALUE;
                for (Skill s : u.getModifiedSkills()){
                  int l = s.getLevel(); 
                  if (l > maxSkill)
                    maxSkill = l;
                  if (l< minSkill)
                    minSkill = l;
                }
                // add all skills weighted by their level
                for (Skill s : u.getModifiedSkills()){
                  su.addLearn(s.getName(), s.getLevel()/(double)maxSkill * (prio-prio2) + prio2);
                }
              } else{
                su.addLearn(talent, prio);
              }
            }
          } catch (Exception e) {
            log.warn("parse error, unit " + u + " line " + o);
            errorFlag = true;
          }
        }
      }
      if (errorFlag) {
        u.addOrder("; $$$ teach error", false, 0);
        u.setOrdersConfirmed(false);
        return null;
      }

      return su;
    }

    public double mainrun() {
      units = (SUnit[]) getUnits().toArray(new SUnit[0]);

      final int numRounds = Math.max(80, (int) (units.length) * 3 / 2);
      final int popSize = numRounds;
      final int numMetaRounds = 3;
      final int select = 5;

      ui.setTitle("");
      ui.setMaximum(numMetaRounds * numRounds + numRounds);
      ui.show();

      Solution[] best = new Solution[numMetaRounds * select * 3 / 2];
      init(best, units);

      for (int metaRound = 0; metaRound < numMetaRounds; ++metaRound) {
        Solution[] population = new Solution[popSize];
        init(population, units);

        for (int round = 0; round < numRounds; ++round) {
          select(population);
          recombine(population);
          if (round % Math.ceil(numRounds / 10) == 0) {
            log.info(round + " 0: " + population[0].evaluate() + " " + population.length / 10
                + ": " + population[population.length / 10].evaluate() + " "
                + (population.length / 10 * 9) + " "
                + population[population.length / 10 * 9].evaluate() + " " + (population.length - 1)
                + ": " + population[population.length - 1].evaluate());
            ui.setProgress((metaRound > 0 ? best[metaRound - 1].evaluate() : "0") + " - "
                + population[0].evaluate(), metaRound * numRounds + round);
            mutate(population, Math.min(1 / Math.log(round + 1), .2));
          } else
            mutate(population, Math.min(.3 / Math.log(round + 1), .2));

        }
        select(population);
        log.info("***" + numRounds + " 0: " + population[0].evaluate() + " " + population.length
            / 10 + ": " + population[population.length / 10].evaluate() + " "
            + (population.length / 10 * 9) + " "
            + population[population.length / 10 * 9].evaluate() + " " + (population.length - 1)
            + ": " + population[population.length - 1].evaluate());
        for (int i = 0; i < select; ++i) {
          best[metaRound * select + i] = population[i];
        }
      }

      select(best);
      log.info(" 0: " + best[0].evaluate() + " l/3: " + best[best.length / 3].evaluate() + " "
          + (best.length - 1) + ": " + best[best.length - 1].evaluate());
      for (int round = 0; round < numRounds * 2; ++round) {
        ui.setProgress("" + best[0].evaluate(), numMetaRounds * numRounds + round);
        select(best);
        recombine(best);
        mutate(best, .1);
      }
      select(best);
      log.info("***** 0: " + best[0].evaluate() + " l/3: " + best[best.length / 3].evaluate() + " "
          + (best.length - 1) + ": " + best[best.length - 1].evaluate());
      ui.ready();
      return setResult(best);
    }

    public void clear() {
      for (Unit u : container.units()) {
        Collection orders = new ArrayList(u.getOrders());
        for (Iterator it = orders.iterator(); it.hasNext();) {
          String o = (String) it.next();
          int start = o.indexOf("$$$");
          if (start != -1) {
            it.remove();
          }
        }
        u.setOrders(orders);
      }
    }

    private void init(Solution[] population, SUnit[] units) {
      for (int i = 0; i < population.length; ++i) {
        population[i] = new Solution(units);
      }
    }

    private void select(Solution[] population) {
      Arrays.sort(population);
    }

    private void mutate(Solution[] population, double d) {
      // keep first individual?
      for (int i = 1; i < population.length / 2; ++i) {
        population[i].mutate(d);
      }

    }

    private void recombine(Solution[] population) {
      int n = population.length / 2;
      int m = population.length - n;
      for (int i = 0; i < n; ++i) {
        int rand1 = random.nextInt(m);
        if (random.nextBoolean()) {
          int rand = random.nextInt(m * (m - 1) / 2);
          rand1 = m - (int) Math.floor(.5 + Math.sqrt(2 * rand));
          rand1 = Math.max(0, Math.min(population.length - 1, rand1));
        }
        int rand2 = random.nextInt(m);
        // log.info(rand1+" + "+rand2+" = "+(population.length - 1 - i));
        population[population.length - 1 - i].mate(population[rand1], population[rand2]);
      }
    }

    private double setResult(Solution[] population) {
      if (population.length == 0)
        return -1;
      Solution best = population[0];
      boolean first = false;
      for (Solution.Info info : best.infos) {
        List<String> orders = new ArrayList<String>(info.getUnit().getOrders());
        for (Iterator it = orders.iterator(); it.hasNext();) {
          String o = (String) it.next();
          if (o.startsWith("LEHRE") || o.startsWith("LERNE")) {
            it.remove();
          }
        }
        info.getUnit().setOrders(orders);
      }
      StringBuffer[] orders = new StringBuffer[best.units.length];
      for (int i = 0; i < best.infos.length; ++i) {
        Solution.Info info = best.infos[i];
        if (info.learning != null)
          orders[i] = new StringBuffer("LERNEN " + info.learning);
        if (info.getTeacher() != -1)
          if (orders[info.getTeacher()] == null)
            orders[info.getTeacher()] = new StringBuffer("LEHRE " + info.getUnit().getID());
          else
            orders[info.getTeacher()].append(" " + info.getUnit().getID());
      }
      for (int i = 0; i < best.infos.length; ++i) {
        Solution.Info info = best.infos[i];
        if (info.learning == null)
          info.getUnit().addOrder("; $$$ T " + info.students, false, 0);
        else
          info.getUnit().addOrder("; $$$ L " + info.getTeacherSet().size(), false, 0);
        if (orders[i] == null)
          info.getUnit().addOrder("; $$$ teaching error", false, 0);
        else {
          info.getUnit().addOrder(orders[i].toString(), false, 0);
          if (info.getTeacher() != -1 || info.students == info.getUnit().getModifiedPersons() * 10)
            info.getUnit().setOrdersConfirmed(true);
          else
            info.getUnit().setOrdersConfirmed(false);
        }
      }

      return best.evaluate();
    }

  }

  public static void foo(final UnitContainer container, final UserInterface ui) {

    (new Teacher(container, ui).new TeachingHelper()).mainrun();

  }

  public static void clear(UnitContainer container) {
    (new Teacher(container, new NullUserInterface()).new TeachingHelper()).clear();

  }

}
