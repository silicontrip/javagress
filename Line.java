import javax.vecmath.Vector3d;
import java.util.ArrayList;

public class Line {
	
	Point o;
	Point d;
	
	//public static final Double earthRadius = 6371.0;
	public static final Double earthRadius = 6367.0;
	
	// close to zero threshold
	final static  double eps = 1E-10;
	
	public Long getdLat() { return d.getLatE6(); }
	public Long getdLng() { return d.getLngE6(); }
	public Long getoLat() { return o.getLatE6(); }
	public Long getoLng() { return o.getLngE6(); }
	
	public Double getoLatAsDouble() { return new Double(o.getLatE6()); }
	public Double getoLngAsDouble() { return new Double(o.getLngE6()); }
	public Double getdLatAsDouble() { return new Double(d.getLatE6()); }
	public Double getdLngAsDouble() { return new Double(d.getLngE6()); }
	
	public Vector3d getoVect() { return o.getVector(); }
	public Vector3d getdVect() { return d.getVector(); }
	
	/*
	public double getoX() { return o.getX(); }
	public double getoY() { return o.getY(); }
	public double getoZ() { return o.getZ(); }
	
	public double getdX() { return d.getX(); }
	public double getdY() { return d.getY(); }
	public double getdZ() { return d.getZ(); }
	*/
	//public void setoLat(Long l) { o.setLat(l); }
	//public void setoLng(Long l) { o.setLng(l); }
	
	public Point getD() { return d; }
	public Point getO() { return o; }
	
	public Line (Point d, Point o)
	{
		this.d = new Point(d);
		this.o = new Point(o);
	}
	
	public Line (Long dla,Long dlo, Long ola,Long olo)
	{
		this.d = new Point(dla,dlo);
		this.o = new Point(ola,olo);
	}
	
	@Override
	public final boolean equals(Object obj2)
	{
  		if (obj2 == this) return true;
  		if (!(obj2 instanceof Line)) return false;
  		Line l = (Line) obj2;
		
		return (this.getO().equals(l.getO()) && this.getD().equals(l.getD())) || (this.getO().equals(l.getD()) && this.getD().equals(l.getO()));
	}
	
	@Override
	public int hashCode()
	{
		return getO().hashCode() | getD().hashCode();
	}
	
	public boolean findIn(Line[] lines)
	{
		for (Line line : lines)
			if (equals(line)) { return true; }
		
		return false;
	}
	
	private Vector3d getGreatCircleIntersection (Line l)
	{
		Vector3d V = this.getNormal();		
		Vector3d U = l.getNormal();		
		Vector3d D = new Vector3d();
		D.cross( V, U );

		if (D.length() == 0)
			return D;
		D.normalize();
		
		return D;
	}


	// determines if this point or it's antipode are nearest this line
	Point pointNear(Point p) { 
		double op = this.getO().getGeoDistance(p);
	//	double dp = this.getD().getGeoDistance(p);
		double oa = this.getO().getGeoDistance(p.inverse());
	//	double da = this.getD().getGeoDistance(p.inverse());
		
	//	System.out.println("P: " + op + ","+dp + "A: "+oa+","+da);

		if (op < oa)
			return p;

		return p.inverse();
		
	}

	int pointOn(Point p) { return pointOn(p.getVector()); }
	

// determines if the point or it's antipode, defined by D is on the current line
	int pointOn (Vector3d D)
	{
		Vector3d S1 = new Vector3d();
		Vector3d S2 = new Vector3d();
		Vector3d V = this.getNormal();
		
	// so what are we working out here?
		S1.cross(getoVect(),V);
		S2.cross(getdVect(),V);
	
		double p0 =-S1.dot(D);
		double p1 = S2.dot(D);

		int zero=0;
		int count=0;

		if (Math.abs(p0) < eps)
			zero++;
		else
			count += Math.signum(p0);

		if (Math.abs(p1) < eps)
			zero++;
		else
			count += Math.signum(p1);

		//System.out.println("Zero: " + zero + " count: " + count);

		if (count==2 || count == -2)  // handles antipodal ???  yes 
			return 1;  // on line

		if (zero>0)
			return 2; // at end

		return 3; // not on line
		/*
		double s0 = -S1.dot(D);
		double s1 = S2.dot(D);
		double s2 = -S3.dot(D);
		double s3 = S4.dot(D);
*/
	}
	
	public int greaterCircleIntersectType (Line l)
	{
		
		Vector3d intPoint = getGreatCircleIntersection(l);		

		int p1 = this.pointOn(intPoint);
		int p2 = l.pointOn(intPoint);

		if (p1==1 && p2 == 1)
			return 1;

		if (p1==0 && p2==0)
			return 0;

		if (p1==0 || p2==0)
			return 3;

		return 2;

	}
	public boolean intersects(Line l) {
		
		// DrawTools dt = new DrawTools();
		//dt.addLine(this);
		//dt.addLine(l);
		
		//int i = intersectType(l);
		int gi = greaterCircleIntersectType(l);
		
	//	System.out.println(">>> Intersects: " + gi);
		
		// really would like some unit tests now.
		return (gi == 1);
	}
	public boolean intersectsOrEqual(Line l) { return (greaterCircleIntersectType(l) != 2);	}
	public boolean equalLine(Line l) { return (greaterCircleIntersectType(l) == 0);	}
	
	public boolean intersects (Line[] lines)
	{
		
		for (Line line : lines)
		{
			if (intersects(line)) { return true; }
		}
		
		return false;
	}
	
	public boolean intersectsOrEquals (Line[] lines)
	{
		for (Line line : lines)
		{
			if (intersectsOrEqual(line)) { return true; }
		}
		
		return false;
	}

	// concept, are we obscured by l or do we obscure l?
	public int obscuredFromBy (Point p, Line l)
	{

			// project line from p to l.o
			// project line from p to l.d

			// are we obscured by l ?
			Line po = new Line(p,l.getO());
			Line pd = new Line(p,l.getD());

			int obscure = 0;
			
			if ( (this.getO().equals(l.getO()) || this.getD().equals(l.getO()) )) obscure = 1;
			if ( (this.getO().equals(l.getD()) || this.getD().equals(l.getD()) )) obscure = 1;

			if (po.intersects(this))
				obscure |= 2;

			if (pd.intersects(this))
				obscure |= 4;

			// partial obscure O
			// partial obscure D
			// total obscure
			// same O
			// same D
			// partial obscure O,D

			return obscure;

	}


	private String da (double[] ip)
	{
		return "[" + ip[0] + ", " + ip[1] + ", " + ip[2] + ", " + ip[3] + "]";
	}
	// return the line subsection (array)
	// that we make on line l from point p
	public ArrayList<Line> shadow (Point p, Line l)
	{

		Line pto = new Line (this.getO(),p.inverse());
		Line ptd = new Line (this.getD(),p.inverse());
		Line plo = new Line (p,l.getO());
		Line pld = new Line (p,l.getD());
	
		ArrayList<Line> al = new ArrayList<Line>();
		
		int p1 = pto.greaterCircleIntersectType(l);
		int p2 = ptd.greaterCircleIntersectType(l);

		System.out.println("pto: " + p1 + " ptd: " + p2);
		System.out.println("[" + this +","+ l + ","+pto+","+ptd+"]");

		if (p1 == 1 && p2 != 1)
		{
			// determine point where pto and l intersect
			Point pp = new Point(l.getGreatCircleIntersection(pto));
			pp = l.pointNear(pp); 
		
			// determine if the new line is o to intersection or d to intersection
			int p3 = plo.greaterCircleIntersectType(this);
			int p4 = pld.greaterCircleIntersectType(this);

			//System.out.println("Shadowed by O end at " + pp + " on point: " + l.pointOn(pp));

			System.out.println("O: o: " + p3 + " p: "+p4 + " ;; " + pp );
			//System.out.println ("[" + plo + "," + pld + "," + this + "]");
			// we should never see both p3 and p4 intersecting
			if (p3==1)
				al.add(new Line(pp,l.getD()));
			else 
			// we assume that p4==1
				al.add(new Line(pp,l.getO()));
		
		}
		if (p2 ==1 && p1 != 1)
		{
		// determine point where ptd and l intersect
			Point pp = new Point(l.getGreatCircleIntersection(ptd));

			pp = l.pointNear(pp); 
			int p3 = plo.greaterCircleIntersectType(this);
			int p4 = pld.greaterCircleIntersectType(this);


			//System.out.println("Shadowed by D end at " + pp + " on " + l.pointOn(pp));
			System.out.println("P: o: " + p3 + " p: "+p4 + " ;; " + pp );
		//System.out.println("O: " + p3 + " P: "+p4);
		//System.out.println ("[" + plo + "," + pld + "," + this + "]");

			if (p3==1)
				al.add(new Line(pp,l.getD()));
			else 
			// we assume that p4==1
				al.add(new Line(pp,l.getO()));
		}
		if (p1==1 && p2 == 1)
		{
			Point pd = new Point(l.getGreatCircleIntersection(ptd));
			pd = l.pointNear(pd);
			Point po = new Point(l.getGreatCircleIntersection(pto));
			po = l.pointNear(po);

			System.out.println("Shadowed subsection at " + pd + " & " + po);
			double pddo = l.getO().getGeoDistance(pd);
			double pddd = l.getD().getGeoDistance(pd);
			double podo = l.getO().getGeoDistance(po);
			double podd = l.getD().getGeoDistance(po);

			if (pddo < pddd)
				al.add(new Line(pd,l.getO()));
			else
				al.add(new Line(pd,l.getD()));
			
			if (podo < podd)
				al.add(new Line(po,l.getO()));
			else
				al.add(new Line(po,l.getD()));

		}
		if (p1!=1 && p2 !=1)
			al.add(l);

		// I just want to say Funky Cole medina, at this point, 
		// this method has been doing my head in for that long.
		// not to mention debugging all the supporting methods in other classes.
		return al;

	}

	private Vector3d getNormal()
	{
		Vector3d A = this.getoVect();
        Vector3d B = this.getdVect();
		
		Vector3d N = new Vector3d();
		N.cross(A,B);  // N = A x B
		N.normalize();
		return N;
	}

	public Double getGeoDistance(Point p) {
		
		Vector3d A = this.getoVect();
                Vector3d B = this.getdVect();
		Vector3d C = p.getVector();

		
		Vector3d N = new Vector3d();
		N.cross(A,B);  // N = A x B
		N.normalize();

		Vector3d F = new Vector3d();
		F.cross(C,N);
		F.normalize();
	
		Vector3d T = new Vector3d();
		T.cross(N,F);
		T.normalize();

		double ac = A.angle(C) * earthRadius;
		double bc = B.angle(C) * earthRadius;
		double tc = T.angle(C) * earthRadius;

		double onSeg = Math.abs(A.angle(B) - A.angle(T) - B.angle(T));

		if (onSeg < eps)
		{
		
		//System.out.println("" + tc + " T: " + onSeg );
		
			return tc;
		}
		else if (ac < bc)
		{
		//System.out.println("" + ac + " A: " + onSeg );
			return ac;
		}
		else
		{
		//System.out.println("" + bc + " B: " + onSeg );
			return bc;
		}
	}
	
	public Double getGeoDistance() {
		return this.getO().getGeoDistance(this.getD());
	}
	
	public String toString() {
		
		return new String ( "{\"type\":\"polyline\",\"latLngs\":[{\"lat\":" + this.getoLat()/1000000.0 +
						   ",\"lng\":" + this.getoLng()/1000000.0 +
						   "},{\"lat\":" + this.getdLat()/1000000.0 +
						   ",\"lng\":" + this.getdLng()/1000000.0 +
						   "}],\"color\":\"#F00000\"}" );
		
		// return new String ( getoLatAsDouble() + "," + getoLngAsDouble() + " - " + getdLatAsDouble() +","+ getdLngAsDouble());
		
	}
	public boolean hasPoint(Point p) {
		return (p.equals(o) || p.equals(d));
	}
	
	public double sign(Point p)
	{
		Vector3d T = new Vector3d();
		Vector3d S = new Vector3d();
		Vector3d R = new Vector3d();

		S.sub(d.getVector(),o.getVector());
		R.sub(p.getVector(),d.getVector());

		T.cross(S,R);
		System.out.println("vector : " + T);
		return (o.getLng() - p.getLng()) * (d.getLat() - p.getLat())  - (d.getLng() - p.getLng()) * (o.getLat() - p.getLat());
	}
	public Double getBearing() { return o.getBearingTo(d); }
	public Double getReverseBearing() { return d.getBearingTo(o); }
	
}
