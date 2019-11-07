# Teacher Plugin

## english

The Teacher Plugin allows automatic assignment of teachers to students. You
have to assign certain special orders to the unit and Teacher will then try to
assign teachers to students in the best possible way and set the units' orders
accordingly.

You have to tell teacher which talents a unit should learn, how much the
different talents are worth and which units should be teachers. Teacher then
tries to maximize the sum of all values of learning units. The values of units
with a teacher are doubled (roughly), because they will learn twice as fast.
You can specify (potential) teachers and students by adding one or more of the
following meta orders to the unit's orders.

    // $$L priority Talent1 target1 max1 Talent2 target2 max2

denotes a student learning two skills of different values. 
    priority denotes the importance of this unit. Units with high priority will be favored when 
             finding teachers. You can start with equal priorities for all units.
    target   denotes the desired skill value
    max      denotes the maximum skill value
  
For example, a unit with "// $$L 100 melee 10 99 endurance 5 99 riding 5 2" will try to keep 
the ration between "melee" and "endurance" at 2:1 and will learn "riding" up to skill level 2.   

    // $$T Talent1 maxDiff1 Talent2 maxDiff2

denotes a teacher that may teach two skills. Students having a skill level differing more
than maxDiff from the teachers skill level are penalized. maxDiff==0 has the special
meaning that there is no such penalty. maxDiff==1 means that the teacher will
not teach this talent.

    // $$T ALLES maxDiff

denotes a teacher teaching all the skills he knows.

    // $$T ALLES 0 melee 2

would also be feasible.

    // $namespace1$T ...

    // $namespace1$L ...

defines an order belonging to a namespace; it can be used to teaching only to
units with certain namespace

It is feasible (in fact, desirable) for a unit to be teacher and student at the
same time. In other words, if a unit has a teaching order it must also have a learning order.

You can initiate teaching via the menu Pluings--Teacher--Teach all for all units. Or via the context
menu of a region in the region tree for just one region. 

## deutsch

Das Teacher-Plugin berechnet automatisch eine Zuweisung von Schülern zu
Lehrern. Um an diesem Prozess teilzunehmen, muss die Einheit bestimmte Befehle
erhalten, die für jedes Talent, das die Einheit lernen soll einen Wert
definieren. Teacher versucht Schülern so Lehrern zuzuweisen, dass die Summe der
Werte aller gelernten Talente maximiert wird. Schüler, die einen Lehrer haben,
werden dabei (ungefähr) doppelt bewertet, da sie Talente doppelt so schnell lernen.

Damit eine Einheit in diesem Prozess abgearbeitet werden kann, müssen ihre
Befehle einen oder mehrere der folgenden Metabefehle enthalten:

    // $$L wert Talent1 ziel1 max1 [Talent2 ziel2 max2]...

steht für einen Schüler, der zwei unterschiedliche Talente mit
unterschiedlichen Werten lernen soll.

    wert    beeinflusst, wie wichtig diese Einheit ist. Einheiten mit hohem Wert werden bei der 
            Vergabe von Lehrern bevorzugt. Sie können für den Anfang einfach alle Werte gleich setzen.
    ziel    ist der angestrebte Talentwert. 
    max     ist der maximale Talentwert.

Eine Einheit mit "// $$L 100 Hiebwaffen 10 99 Ausdauer 5 99 Reiten 5 2" wird versuchen das 
Verhältnis zwischen ihrem Hiebwaffentalent und dem Ausdauertalent bei etwa 2:1 zu halten. Reiten 
wird sie bis Maximal Stufe 2 lernen.
   
    // $$T Talent1 maxDiff1 [Talent2 maxDiff2]...

steht für einen Lehrer, der zwei Talente lehrt. Schüler, deren Talentwert um
mehr als maxDiff vom Talentwert des Lehrers abweicht, werden gering bewertet.
maxDiff==0 bedeutet, dass der Talentunterschied egal ist. maxDiff==1 bedeutet,
dass der Lehrer dieses Talent nicht lehren wird.

    // $$T ALLES maxDiff

steht für einen Lehrer, der alle ihm bekannten Talente lehrt

    // $$T ALLES 0 Hiebwaffen 2

ist auch zulässig.

    // $namespace1$T ...

    // $namespace1$L ...

definiert einen Metabefehl, der zu einem sog. Namensraum gehört. Man kann den
Einflussbereich der automatischen Lehre auf Einheiten in einem bestimmten
Namensraum eingrenzen.

Eine Einheit, die Lehrer ist muss auch mindestens ein Talent lernen.

Über das Menü Plugins--Lehrer--Alle lehren wird die Lehre für alle Einheiten angestoßen. Alternativ
kann man über das Kontextmenü einer Region im Regionsbaum die Lehre nur für eine Region asntoßen.

