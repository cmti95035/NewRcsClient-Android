package mobisocial.musubi.cloudstorage;

import android.content.Context;

import org.json.JSONObject;

import mobisocial.musubi.model.MObject;


public class Dropbox implements CloudStorage {

	
	private static final String dropboxAppKey = "3bduqwpgg381rlj";
    private static final String dropboxAppSecret = "h8a2ka5f2vm8ttk";

	public Dropbox(){
		
	}

	@Override
	public void setAccount(Context context) {

	}

	@Override
	public void login(Context context,int resultcode) {
	//	mDbxAcctMgr.startLink((Activity)context, resultcode);
	}

	@Override
	public void saveMessages(final MObject object,Context context) {
		
	}

	public boolean hasLinkedAccount() {
	//	return mDbxAcctMgr.hasLinkedAccount();
		return false;
	}

	public void logout(Context context) {
	//	mDbxAcctMgr.unlink();
	}

	@Override
	public void saveMessages(final MObject object) {

/*		Thread workThread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Log.e("Dropbox","start to save");
					
		            final String TEST_DATA = object.GetCloudJson().toString();
		            final String TEST_FILE_NAME = object.feedId_+"/"+object.timestamp_+".json";
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
		            

		    		if(object.type_.equals(PictureObj.TYPE)){
		    			String imagepath;
						try {
							imagepath = new JSONObject(object.json_).getString("AppPath");
							Log.e("dropbox",imagepath);
							//String imagepath = UriToFilePath(uri,context);
							//Log.e("BAIdu",imagepath);
							File image = new File(imagepath);
							
							String imagename = image.getName();
							String prefix=imagename.substring(imagename.lastIndexOf(".")+1); 
							String TEST_IMAGE_NAME = object.feedId_+"/"+object.timestamp_+"."+prefix;
							Log.e("dropbox",TEST_IMAGE_NAME);
							DbxPath testPath1 = new DbxPath(DbxPath.ROOT, TEST_IMAGE_NAME);
							
							if (!dbxFs.exists(testPath1)) {
				                DbxFile testFile = dbxFs.create(testPath1);
				                try {
				                    testFile.writeFromExistingFile(image, false);
				                } finally {
				                    testFile.close();
				                }
				               // mTestOutput.append("\nCreated new file '" + testPath + "'.\n");
				            }
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
		    		}
		            } catch (IOException e) {
		        }
			}
			});
			workThread.start();*/
	}

	@Override
	public boolean hasLinkedAccount(Context mContext) {
		//return mDbxAcctMgr.hasLinkedAccount();
		return false;
	}

	@Override
	public void saveMessages(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	public void saveImages(MObject object, String absolutePath) {
		// TODO Auto-generated method stub
		
	}
}
