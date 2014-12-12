package mobisocial.musubi.cloudstorage;

import java.io.IOException;
import java.util.List;

import mobisocial.musubi.model.MObject;

import org.json.JSONObject;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;


public class Dropbox implements CloudStorage {

	
	private static final String dropboxAppKey = "m2xgu9kayi3gzlp";
    private static final String dropboxAppSecret = "leikpr9er6j71pw";
	private DbxAccountManager mDbxAcctMgr;
	
	public Dropbox(){
		
	}
	
	@Override
	public void SetAccount(Context context) {
		// TODO Auto-generated method stub
		mDbxAcctMgr = DbxAccountManager.getInstance(context, dropboxAppKey, dropboxAppSecret);
	}
	
	@Override
	public void Login(Context context,int resultcode) {
		// TODO Auto-generated method stub
		mDbxAcctMgr.startLink((Activity)context, resultcode);
	}
	
	@Override
	public void SaveMeseages(MObject object,Context context) {
		// TODO Auto-generated method stub
		try {
            final String TEST_DATA = object.toString();
            final String TEST_FILE_NAME = "test.txt";
            DbxPath testPath = new DbxPath(DbxPath.ROOT, TEST_FILE_NAME);

            // Create DbxFileSystem for synchronized file access.
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());

            // Print the contents of the root folder.  This will block until we can
            // sync metadata the first time.
            List<DbxFileInfo> infos = dbxFs.listFolder(DbxPath.ROOT);
            //mTestOutput.append("\nContents of app folder:\n");
            for (DbxFileInfo info : infos) {
                //mTestOutput.append("    " + info.path + ", " + info.modifiedTime + '\n');
            }

            // Create a test file only if it doesn't already exist.
            if (!dbxFs.exists(testPath)) {
                DbxFile testFile = dbxFs.create(testPath);
                try {
                    testFile.writeString(TEST_DATA);
                } finally {
                    testFile.close();
                }
               // mTestOutput.append("\nCreated new file '" + testPath + "'.\n");
            }

            // Read and print the contents of test file.  Since we're not making
            // any attempt to wait for the latest version, this may print an
            // older cached version.  Use getSyncStatus() and/or a listener to
            // check for a new version.
           /* if (dbxFs.isFile(testPath)) {
                String resultData;
                DbxFile testFile = dbxFs.open(testPath);
                try {
                    resultData = testFile.readString();
                } finally {
                    testFile.close();
                }
                //mTestOutput.append("\nRead file '" + testPath + "' and got data:\n    " + resultData);
            } else if (dbxFs.isFolder(testPath)) {
                //mTestOutput.append("'" + testPath.toString() + "' is a folder.\n");
            }
            dd*/        
            } catch (IOException e) {
            //mTestOutput.setText("Dropbox test failed: " + e);
        }
	}

	public boolean hasLinkedAccount() {
		// TODO Auto-generated method stub
		return mDbxAcctMgr.hasLinkedAccount();
	}

	public void Logout(Context context) {
		// TODO Auto-generated method stub
		mDbxAcctMgr.unlink();
	}

	@Override
	public void SaveMeseages(MObject object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasLinkedAccount(Context mContext) {
		// TODO Auto-generated method stub
		return mDbxAcctMgr.hasLinkedAccount();
	}

	@Override
	public void SaveMeseages(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	public void SaveImages(MObject object, String absolutePath) {
		// TODO Auto-generated method stub
		
	}

}
