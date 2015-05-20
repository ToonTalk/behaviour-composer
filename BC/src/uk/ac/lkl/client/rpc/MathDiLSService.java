package uk.ac.lkl.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MathDiLSService extends RemoteService {
    public String DDAFileRead(int fileID);

    public String[] listDDAFiles();

    public String insertDDAFile2(String title, String description, String UUEncodedcontent);
}
