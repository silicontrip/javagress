 
public class Field {


	Long[] lat;
	Long[] lng;	
	
	Portal[] portals;
	
	public Field () {
		lat = new Long[3];
		lng = new Long[3];
		portals = new Portal[3];
	}
		
	public Field (Point p0, Point p1, Point p2) {
		this();
		
		setLat(0,p0.getLat());
		setLat(1,p1.getLat());
		setLat(2,p2.getLat());
		
		setLng(0,p0.getLng());
		setLng(1,p1.getLng());
		setLng(2,p2.getLng());
		
	}
	
	public Field (Portal p0, Portal p1, Portal p2) {
		this(p0.getPoint(),p1.getPoint(),p2.getPoint());
		portals[0] = p0;
		portals[1] = p1;
		portals[2] = p2;
	}
	
	public Portal getPortal(int index)
	{
		return portals[index];
	}
	
	public void setLat(int index, Long l)
	{
		if (index >=0 && index <= 2) 
		{
			lat[index] = l;
		}
	}
	
	public void setLng(int index, Long l)
	{
		if (index >=0 && index <= 2) 
		{
			lng[index] = l;
		}
	}
	
	public Long getLat(int index)
	{
		if (index >=0 && index <= 2) 
		{
			return lat[index];
		}
		return 0L;
	}

	public Long getLng(int index)
	{
		if (index >=0 && index <= 2) 
		{
			return lng[index];
		}
		return 0L;
	}
	
	public Double getArea () 
	{
	
		return (Math.abs(lng[0] * (lat[1] - lat[2]) +
				   lng[1] * (lat[2] - lat[0]) +
				   lng[2] * (lat[0] - lat[1])))/200000.0;
		
	}
	
	
	public Double getGeoArea () 
	{

		double a = getLine(0).getGeoDistance();
		double b = getLine(1).getGeoDistance();
		double c = getLine(2).getGeoDistance();
		
		//System.err.println("length: " + a + " - " + b + " - " + c);
		
		double s = ( a + b + c ) / 2.0;
		
		return Math.sqrt(s * (s-a) * (s-b) * (s-c));
		
	}
	
    public Double getGeoPerimeter() {
        return 		getLine(0).getGeoDistance() + getLine(1).getGeoDistance() +getLine(2).getGeoDistance();
    }
	
	public Line getLine(int index) {
		
			
			if (index == 0) {
				return new Line (getLat(0),getLng(0),getLat(1),getLng(1));
			}
		if (index == 1)
		{
			return new Line (getLat(1),getLng(1),getLat(2),getLng(2));
		}
	
		if (index == 2)
		{
			return new Line (getLat(2),getLng(2),getLat(0),getLng(0));
		}

		return null;
		
		
	}
	
	public boolean intersects(Field f)
	{
		
		boolean intersect = f.getLine(0).intersects(getLine(0)) ||
		f.getLine(0).intersects(getLine(1)) ||
		f.getLine(0).intersects(getLine(2)) ||
		f.getLine(1).intersects(getLine(0)) ||
		f.getLine(1).intersects(getLine(1)) ||
		f.getLine(1).intersects(getLine(2)) ||
		f.getLine(2).intersects(getLine(0)) ||
		f.getLine(2).intersects(getLine(1)) ||
		f.getLine(2).intersects(getLine(2));
	
		/*
		if (!intersect)
		{		
			System.err.println("[" + getLine(0) + "," + getLine(1) + "," + getLine(2) + "," +
								   f.getLine(0) + "," + f.getLine(1) + "," + f. getLine(2) + "]" );
		}
		*/
		
		return intersect;

		
	}
	
    
    
	protected double sign (double p1a, double p1o, double p2a, double p2o, double p3a, double p3o)
	{
	
		return (p1o - p3o) * (p2a - p3a) - (p2o - p3o) * (p1a - p3a);
		
	}
	
	public boolean inside (Point p)
	{
		boolean b1 = sign (p.getLat(),p.getLng(),lat[0],lng[0],lat[1],lng[1]) <= 0.0;
		boolean b2 = sign (p.getLat(),p.getLng(),lat[1],lng[1],lat[2],lng[2]) <= 0.0;
		boolean b3 = sign (p.getLat(),p.getLng(),lat[2],lng[2],lat[0],lng[0]) <= 0.0;
		
		return (b1 == b2) && (b2 == b3);
		
	}
	
	public boolean equals(Field f) 
	{
	
		return (((f.getLat(0) == this.getLat(0) && f.getLng(0) == this.getLng(0)) ||
				(f.getLat(0) == this.getLat(0) && f.getLng(1) == this.getLng(1)) ||
				(f.getLat(0) == this.getLat(0) && f.getLng(2) == this.getLng(2))) &&
				
				((f.getLat(1) == this.getLat(1) && f.getLng(1) == this.getLng(1)) ||
				 (f.getLat(1) == this.getLat(1) && f.getLng(2) == this.getLng(2)) ||
				 (f.getLat(1) == this.getLat(1) && f.getLng(0) == this.getLng(0))) &&
				 ((f.getLat(2) == this.getLat(2) && f.getLng(2) == this.getLng(2)) ||
				  (f.getLat(2) == this.getLat(2) && f.getLng(0) == this.getLng(0)) ||
				 (f.getLat(2) == this.getLat(2) && f.getLng(1) == this.getLng(1))));
	
				}
	
	public String toString() { return this.getDraw(); } 
	
	public String getDraw() 
	{
		
		return new String ( "{\"type\":\"polygon\",\"latLngs\":[{\"lat\":" + this.getLat(0)/1000000.0 + 
						   ",\"lng\":" + this.getLng(0)/1000000.0 +
						   "},{\"lat\":" + this.getLat(1)/1000000.0 +
						   ",\"lng\":" + this.getLng(1)/1000000.0 +
						   "},{\"lat\":" + this.getLat(2)/1000000.0 +
						   ",\"lng\":" + this.getLng(2)/1000000.0 +
						   "}],\"color\":\"#a05000\"}" );
		
	}
		
}
