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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

public class TeacherNew {

  magellan.library.Unit unit;
  magellan.library.GameData world;
  magellan.library.UnitContainer container;
  private UserInterface ui = new NullUserInterface();

  TeacherNew(UnitContainer container, UserInterface ui) {
    this.container = container;
    this.ui = ui;
  }

  class TeachingHelper {
    private final Logger log = Logger.getInstance(TeachingHelper.class);

    private SUnit[] units;
    java.lang.String delim = " ";

    Random random = new Random();

    public class SUnit {

      magellan.library.Unit unit;
      Map teach = new HashMap();
      Map learn = new HashMap();

      SUnit(magellan.library.Unit u) {
        unit = u;
      }

      public void addTeach(String talent, Double prio) {
        teach.put(talent, prio);
      }

      public void addLearn(String talent, Double prio) {
        learn.put(talent, prio);
      }

      public magellan.library.Unit getUnit() {
        return unit;
      }

      public double getPrio(String learning) {
        if (learning == null)
          return 0;
        return (Double) (learn.get(learning) != null ? learn.get(learning) : -1);
      }

      public Set getTalents() {
        return learn.keySet();
      }

      public int getPersons() {
        return getUnit().getModifiedPersons();
      }
      
    }

    class Solution implements Comparable {

      class Info {
        private SUnit unit = null;

        private String learning = null;
        private int numStudents = 0;
        private int numTeachers = 0;

        Set<Unit> teacherSet = new HashSet<Unit>();
        Set<Unit> studentSet = new HashSet<Unit>();

        public Info(SUnit unit) {
          this.unit = unit;
          init();
          assignLearn();
        }

        public void init() {
          learning = null;
          numStudents = 0;
          numTeachers = 0;
          teacherSet.clear();
          studentSet.clear();
        }

        public SUnit getSUnit() {
          return unit;
        }

        public Unit getUnit() {
          return unit.getUnit();
        }

        private int getPersons() {
          return getUnit().getModifiedPersons();
        }

        public void assignLearn() {
          if (numStudents != 0)
            clearStudents();
          learning = (String) unit.getTalents().toArray()[random.nextInt(unit.getTalents().size())];
        }

        public int getStudents() {
          return numStudents;
        }

        public Collection<Unit> getStudentSet() {
          return studentSet;
        }

        public void clearStudents() {
          for (Iterator<Unit> studentIt = studentSet.iterator(); studentIt.hasNext();) {
            Info student = getInfo(studentIt.next());
            studentIt.remove();
            student.removeTeacher(this, false);
          }
          numStudents = 0;
        }

        public void removeStudent(Unit student) {
          removeStudent(getInfo(student), true);
        }

        private void removeStudent(Info student, boolean propagate) {
          if (studentSet.contains(student.getUnit())) {
            studentSet.remove(student.getUnit());
            numStudents = Math.max(0, numStudents - student.getPersons());
            if (propagate)
              student.removeTeacher(this, false);
          } else {
            log.warn(student + " not student of " + this);
          }
        }

        public void addStudent(Unit student) {
          addStudent(getInfo(student), true);
        }

        private void addStudent(Info student, boolean propagate) {
          if (studentSet.contains(student.getUnit()))
            log.warn(student.getUnit() + " already student of " + this);
          else {
            studentSet.add(student.getUnit());
            numStudents += student.getPersons();
            if (propagate) {
              student.addTeacher(this, false);
            }
          }
          if (studentSet.size()>20)
            log.warn("many students: "+this);
        }

        public int getNumStudents() {
          return numStudents;
        }

        public void clearTeachers() {
          for (Iterator<Unit> teacherIt = teacherSet.iterator(); teacherIt.hasNext();) {
            Info teacher = getInfo(teacherIt.next());
            teacherIt.remove();
            teacher.removeStudent(this, false);
          }
          numTeachers = 0;
        }

        public void removeTeacher(Unit teacher) {
          removeTeacher(getInfo(teacher), true);
        }

        private void removeTeacher(Info teacher, boolean propagate) {
          if (teacherSet.contains(teacher.getUnit())) {
            teacherSet.remove(teacher.getUnit());
            numTeachers = Math.max(0, numTeachers - teacher.getPersons());
            if (propagate)
              teacher.removeStudent(this, false);
          } else {
            log.warn(teacher + " not teacher of " + this);
          }
        }

        public void addTeacher(Unit teacher) {
          addTeacher(getInfo(teacher), true);
        }

        private void addTeacher(Info teacher, boolean propagate) {
          if (teacherSet.contains(teacher.getUnit()))
            log.warn(teacher + " already teacher of " + this);
          else {
            teacherSet.add(teacher.getUnit());
            numTeachers += teacher.getPersons();
            if (propagate) {
              teacher.addStudent(this, false);
            }
          }
          if (teacherSet.size()>1)
            log.warn("many teachers");
        }

        public int getNumTeachers() {
          return numTeachers;
        }

        public Collection<Unit> getTeacherSet() {
          return teacherSet;
        }

        public String toString() {
          StringBuffer result = new StringBuffer(getUnit().toString());
          if (learning!=null){
            result.append(" learning " + learning + " teachers:");
            for (Unit u: getTeacherSet())
              result.append(" "+u.getID());
          } else {
            result.append(" teaching:");
            for (Unit u: getStudentSet())
              result.append(" "+u.getID());
          }
          return result.toString();
        }

      }

      SUnit[] units;
      Info[] infos;

      boolean changed = true;
      double result = 0;
      private Map<Unit, Info> infoMap = new HashMap<Unit, Info>();

      public Solution(SUnit[] units) {
        this.units = units;
        init();
      }

      void init() {
        this.infos = new Info[this.units.length];
        this.infoMap.clear();
        for (int i = 0; i < infos.length; ++i) {
          Info info = new Info(units[i]);
          infos[i] = info;
          infoMap.put(info.getUnit(), info);
        }
        changed = true;
        result = 0;
      }

      void clear() {
        for (Info info : infos)
          info.init();
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
          if (info.learning != null) {
            if (su.getPrio(info.learning) > 0)
              result +=
                  ((1 + Math.min(1, info.getNumTeachers() / info.getPersons())))
                      * su.getPrio(info.learning) * su.getPersons();
          }
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
              } else {
                // become teacher
                info.learning = null;
                info.clearTeachers();
              }
            }

            // assign new teacher
            if (info.learning != null && random.nextDouble() < teacherProb) {
              info.clearTeachers();
            }
          }
        }

        assignTeachers();
        changed = true;
      }

      protected void assignTeachers() {
        Collection<Unit> teacherSet = new ArrayList<Unit>();

        // reset teachers
        for (int i = 0; i < infos.length; ++i) {
          Info info = infos[i];
          if (info.learning == null && info.getNumStudents() < 10 * info.getPersons()) {
            teacherSet.add(info.getUnit());
          }
          if (!info.getTeacherSet().isEmpty())
            for (Unit u : info.getTeacherSet()){
              if ( getInfo(u).learning != null)
                log.warn("unexpected error");
            }
            
        }

        if (teacherSet.size() <= 0)
          return;

        Unit[] teachers = teacherSet.toArray(new Unit[0]);

        // assign teachers (one per student)
        for (int i = 0; i < infos.length; ++i) {
          Info student = infos[i];
          if (student.learning != null) {
            if (student.getTeacherSet().isEmpty()) {
              // find new teacher
              int firstTeacher = random.nextInt(teachers.length);
              for (int j = 0; j < teachers.length; ++j) {
                int current = (firstTeacher + j) % teachers.length;
                // suitable teacher?
                if (validTeacher(student, teachers[current])) {
                  assignTeacher(student, teachers[current]);
                  break;
                }
              }
            }
          }
        }
        // // assign teachers, 2nd try
        // for (int i = 0; i < infos.length; ++i) {
        // Info student = infos[i];
        // if (student.learning != null) {
        // if
        // (student.getNumTeachers()*10<student.getUnit().getModifiedPersons())
        // {
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
        clear();

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
          info.init();
          info.learning = parent.learning;
        }

        // assign teachers
        for (int i = 0; i < infos.length; ++i) {
          Info student = infos[i];
          Info parent;
          if (parents[i] % 2 == 0)
            parent = solution.infos[i];
          else
            parent = solution2.infos[i];
          if (student.learning != null && student.learning == parent.learning) {
            if (parent.getTeacherSet().size() == 1)
              for (Unit t : parent.getTeacherSet()) {
                if (validTeacher(student, t, true))
                  assignTeacher(student, t);
//                else
//                  log.warn("teacher not valid");
              }
          }
        }
        assignTeachers();
        changed = true;
      }

      private Info getInfo(Unit u) {
        return infoMap.get(u);
      }

      private boolean validTeacher(Info student, Unit teacher) {
        return validTeacher(student, teacher, false);
      }

      private boolean validTeacher(Info student, Unit teacher, boolean partial) {
        if (getInfo(teacher).learning != null
            || getLevel(teacher, student.learning) < getLevel(student.getUnit(),
                student.learning) + 2)
          return false;
        else
          return partial
              || teacher.getModifiedPersons() * 10 >= getInfo(teacher).getNumStudents()
                  + student.getUnit().getModifiedPersons();

      }

      public void assignTeacher(Info student, Unit teacher) {
        student.addTeacher(teacher);
      }
    }

    /**
     * Returns the skill level of a unit. For example
     * getLevel(unit,"Unterhaltung")
     */
    public int getLevel(Unit unit, String skillName) {
      Collection<Skill> skills = unit.getSkills();
      if (skills != null) {
        for (Skill skill : skills) {
          if (skill.getSkillType().getName().equalsIgnoreCase(skillName)) {
            return skill.getLevel();
          }
        }
      }
      return 0;
    }

    public java.util.Set getUnits() {
      java.util.Set result = new java.util.HashSet();
      for (magellan.library.Unit u : container.units()) {
        SUnit su = parseUnit(u);
        if (su != null)
          result.add(su);
      }
      return result;
    }

    public SUnit parseUnit(magellan.library.Unit u) {
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
              double prio = Double.parseDouble(st.nextToken());
              if (su == null)
                su = new SUnit(u);
              su.addTeach(talent, prio);
            }
          } catch (Exception e) {
            log.warn("parse error, unit " + u + " line " + o);
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
              double prio = Double.parseDouble(st.nextToken());
              if (su == null)
                su = new SUnit(u);
              su.addLearn(talent, prio);
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
      for (magellan.library.Unit u : container.units()) {
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
        Collection orders = new ArrayList(info.getUnit().getOrders());
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
        else {
          orders[i] = new StringBuffer("LEHRE");
          for (Unit student : info.getStudentSet())
            orders[i].append(" " + student.getID());
        }
      }
      for (int i = 0; i < best.infos.length; ++i) {
        Solution.Info info = best.infos[i];
        if (info.learning == null)
          info.getUnit().addOrder("; $$$ T " + info.getNumStudents(), false, 0);
        else
          info.getUnit().addOrder("; $$$ L "+info.getTeacherSet().size(), false, 0);
        if (orders[i] == null)
          info.getUnit().addOrder("; $$$ teaching error", false, 0);
        else {
          info.getUnit().addOrder(orders[i].toString(), false, 0);
          if (info.getNumTeachers() * 10 >= info.getUnit().getModifiedPersons()
              || info.getNumStudents() == info.getUnit().getModifiedPersons() * 10)
            info.getUnit().setOrdersConfirmed(true);
          else
            info.getUnit().setOrdersConfirmed(false);
        }
      }

      return best.evaluate();
    }

  }

  public static void foo(final UnitContainer container, final UserInterface ui) {

    (new TeacherNew(container, ui).new TeachingHelper()).mainrun();

  }

  public static void clear(UnitContainer container) {
    (new TeacherNew(container, new NullUserInterface()).new TeachingHelper()).clear();

  }

}
