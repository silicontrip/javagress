public class draw {

    public static void main(String[] args) {	

	Arguments ag = new Arguments(args);

	try {
		DrawTools dt = new DrawTools( ag.getArgumentAt(0));
		
		System.out.println ( dt.getMu() + " (" + dt.size() + ") : " + dt);
       } catch (Exception e) {

                System.out.print ("Exception: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
        }
}
}
