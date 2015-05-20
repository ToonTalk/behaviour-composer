package uk.ac.lkl.server.persistent;

public class UsageStatistics {
    
    public int loadCount;
    public int runCount;

    public UsageStatistics(int loadCount, int runCount) {
	this.loadCount = loadCount;
	this.runCount = runCount;
    }

    public UsageStatistics() {
	this(0, 1);
    }

}
