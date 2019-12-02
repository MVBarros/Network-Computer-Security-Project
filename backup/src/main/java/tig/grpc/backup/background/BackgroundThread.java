package tig.grpc.backup.background;

import tig.grpc.backup.dao.FileDAO;

public class BackgroundThread implements Runnable {

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000 * 60 * 60 * 24 * 7);
            } catch (InterruptedException e) {
                //Should never happen
                e.printStackTrace();
            }
            FileDAO.deleteOldFiles();
        }
    }
}
