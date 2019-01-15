public class RunTimer {

	long startTime;
	long splitTime;
        private static final double nanoPerSec = 1000000000.0;


public void start() {startTime = System.nanoTime(); splitTime = startTime;}
public double split() { 
	long endTime = System.nanoTime();
	double elapsedTime = (endTime - splitTime) / nanoPerSec;
	splitTime = endTime;
	return elapsedTime;
}
public double stop() {
	long endTime = System.nanoTime();
	double elapsedTime = (endTime - startTime) / nanoPerSec;
	return elapsedTime;
}

}
