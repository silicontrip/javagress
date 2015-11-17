class testLine {

public static void main(String[] args) {

	Line l1 = new Line (-37000000L,144500000L,-37000000L,145500000L);
	Line l2 = new Line (-36500000L,145000000L,-37500000L,145000000L);
	Line l3 = new Line (-36500000L,145000000L,-35500000L,145000000L);
	Line l4 = new Line (-37000000L,145500000L,-37000000L,144500000L);
	Line l5 = new Line (-39000000L,145500000L,-37000000L,144500000L);

	Line l6 = new Line (0L,170000000L,0L,190000000L);
	Line l7 = new Line (0L,180000000L,-45000000L,180000000L);


	Line l8 = new Line ( -37876987L,145287126L , -37839598L,145287126L);
	Line l9 = new Line (-37816232L,145192321L,-37816784L,145192659L);


	//intersects
	System.out.println ("1==" +l1.greaterCircleIntersectType(l2));
	// not intersects
	System.out.println ("2==" +l1.greaterCircleIntersectType(l3));
	// equal
	System.out.println ("0==" +l1.greaterCircleIntersectType(l4));
	// touches at end
	System.out.println ("3==" +l1.greaterCircleIntersectType(l5));
	System.out.println ("3==" +l5.greaterCircleIntersectType(l1));
	// touches at middle
	System.out.println ("3==" +l6.greaterCircleIntersectType(l7));
	System.out.println ("3==" +l7.greaterCircleIntersectType(l6));

	DrawTools dt = new DrawTools();
	dt.addLine(l8);
	dt.addLine(l9);
	System.out.println(dt.out());
	System.out.println ("2==" +l9.greaterCircleIntersectType(l8));


}

}
