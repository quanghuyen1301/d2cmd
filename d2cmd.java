import com.dropbox.core.*;

import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class d2cmd {

	public static String FILECMD = "filecmd";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	/**
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		String args_1 = "client";
		if (args.length > 0) {
			args_1 = args[0];
		}

		d2cmd(args_1);
	}

	public static void d2cmd(String args_1) throws Throwable {

		String APP_KEY;// = "d25fdq0wyps****";
		String APP_SECRET;// = "2ig70dmxhxg****";
		String accessToken;
		String FILEKEY = "key";
		List<DbxEntry.File> fileinfo = null;
		String File2Key = ReadFile(FILEKEY);

		if (File2Key.equals("")) {
			Info("Please create filename : " + FILEKEY + "APP_KEY:APP_SECRET");
			return;
		}
		String[] File2KeyArraySlipt = File2Key.split(":");
		if (File2KeyArraySlipt.length == 2) {
			APP_KEY = File2KeyArraySlipt[0];
			APP_SECRET = File2KeyArraySlipt[1];
			
		} else if (File2KeyArraySlipt.length == 3) {
			APP_KEY = File2KeyArraySlipt[0];
			APP_SECRET = File2KeyArraySlipt[1];
		} else {
			Info("Please check filename : " + FILEKEY + "APP_KEY:APP_SECRET");
			return;
		}

		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

		DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());

		if (File2KeyArraySlipt.length == 2) {
			DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
			// Have the user sign in and authorize your app.
			String authorizeUrl = webAuth.start();
			System.out.println("1. Go to: " + authorizeUrl);
			System.out.println("2. Click \"Allow\" (you might have to log in first)");
			System.out.println("3. Input authorization code : ");
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			DbxAuthFinish authFinish = webAuth.finish(code);
			// System.out.println("accessToken = " + authFinish.accessToken);
			File2Key = File2Key + ":" + authFinish.accessToken;
			WriteFile(FILEKEY, File2Key);
			accessToken = authFinish.accessToken;
		} else {
			accessToken = File2KeyArraySlipt[2];
		}

		DbxClient client = new DbxClient(config, accessToken);
//		if(client.getAccountInfo() == null){
//			Debug("!!!!");
//			return ;
//		}
		Debug("Linked account: " + client.getAccountInfo().displayName);


		if (args_1.endsWith("client")) {
			while (true) {
				String strStream = "";
				do {
					do {
						System.out.print(ANSI_BLUE + "shell:" + ANSI_RESET);
						strStream = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
					} while (strStream.equals(""));

				} while (LocalCommand(client,strStream));
				
				DropboxUploadString(client,strStream,FILECMD);
				System.out.println("send command done...wait repont");
				
				DropboxWaitNewVerstion(client,FILECMD);
				
				DropboxDownloadFile(client,FILECMD,FILECMD);
				String shell2txt = ReadFile(FILECMD);
				Debug(shell2txt);

			}
		}

		if (args_1.endsWith("server")) {
			while (true) {
				DropboxWaitNewVerstion(client,FILECMD);
				Debug("Download file");
				DropboxDownloadFile(client,FILECMD,FILECMD);
				String shell2txt = ReadFile(FILECMD);
				Debug("Command : " + shell2txt);
				if(!ServerCommand(client,shell2txt))
				{
					String ServerSentResult = "";
					if (!shell2txt.equals("")) {
						Runtime r = Runtime.getRuntime();
						Process p = r.exec(shell2txt);
						p.waitFor();
						BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String line = "";
						while ((line = b.readLine()) != null) {
							ServerSentResult = ServerSentResult + line + "\n";
						}
						b.close();
					} else {
						ServerSentResult = "Command null";
					}
					DropboxUploadString(client, ServerSentResult, FILECMD);
				}

			}
		}

	}
	public static boolean DropboxWaitNewVerstion(DbxClient client, String DBFileName) {
		if(!DBFileName.startsWith("/")){
			DBFileName = "/" + DBFileName;
		}
		List<DbxEntry.File> DBfileinfo = null;
		try {
			DBfileinfo = client.getRevisions(DBFileName);
		} catch (DbxException e) {
			return false;
		}

		while (true) {
			List<DbxEntry.File> fileinfonew;
			try {
				fileinfonew = client.getRevisions(DBFileName);
				if (!DBfileinfo.get(0).lastModified.toGMTString().endsWith(fileinfonew.get(0).lastModified.toGMTString()))
					break;
			} catch (DbxException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	public static void DropboxDownloadFile(DbxClient client, String DBFileName,String localFileName) {

		if(!DBFileName.startsWith("/")){
			DBFileName = "/" + DBFileName;
		}
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(localFileName);
			client.getFile(DBFileName, null, outputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	public static void DropboxUploadFile(DbxClient client, String LocalFileName,String DBFileName) {

		
		if(!DBFileName.startsWith("/")){
			DBFileName = "/" + DBFileName;
		}
		List<DbxEntry.File> fileinfo = null;
		try {
			fileinfo = client.getRevisions(DBFileName);
		} catch (DbxException e) {
		}

		try {
			fileinfo = client.getRevisions(DBFileName);
		} catch (DbxException e) {
		}

		File inputFile = new File(LocalFileName);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputFile);
			if (fileinfo == null) {
				client.uploadFile(DBFileName, DbxWriteMode.add(), inputFile.length(), inputStream);
			} else {
				client.uploadFile(DBFileName, DbxWriteMode.update(fileinfo.get(0).rev), inputFile.length(), inputStream);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} finally {
//			try {
				//inputStream.close();
//			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	public static void DropboxUploadString(DbxClient client, String LocalString,String DBFileName) {

		InputStream stream = new ByteArrayInputStream(LocalString.getBytes());
				
		if(!DBFileName.startsWith("/")){
			DBFileName = "/" + DBFileName;
		}
		List<DbxEntry.File> fileinfo = null;
		try {
			fileinfo = client.getRevisions(DBFileName);
		} catch (DbxException e) {
		}

		try {
			fileinfo = client.getRevisions(DBFileName);
		} catch (DbxException e) {
		}

		try {
			if (fileinfo == null) {
				client.uploadFile(DBFileName, DbxWriteMode.add(), LocalString.length(), stream);
			} else {
				client.uploadFile(DBFileName, DbxWriteMode.update(fileinfo.get(0).rev), LocalString.length(), stream);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} finally {
		}
	}
	public static boolean ServerCommand(DbxClient client,String CommandInput) {
		String DEFINE_LOCAL = "local";
		if (!CommandInput.startsWith(DEFINE_LOCAL)) {
			return false;
		}
		String [] CommandInputArray = CommandInput.split(" ");
		if(CommandInputArray.length == 4)
		{
//			adb pull <remote> <local>
			if(CommandInputArray[1].endsWith("pull")){
//				DropboxDownloadFile();
				DropboxUploadString(client, "pull done", FILECMD);
				return true;
			}
			if(CommandInputArray[1].endsWith("push")){
				Debug("Server local");
//				DropboxUploadFile(client,CommandInputArray[2],CommandInputArray[3]);
				DropboxDownloadFile(client, CommandInputArray[3], CommandInputArray[2]);
				DropboxUploadString(client, "Push Ok", FILECMD);
				return true;
			}
		}
		return true;
	}
	public static boolean LocalCommand(DbxClient client,String CommandInput) {
		String DEFINE_LOCAL = "local";
		if (!CommandInput.startsWith(DEFINE_LOCAL)) {
			return false;
		}
		String [] CommandInputArray = CommandInput.split(" ");
		if(CommandInputArray.length == 4)
		{
//			adb pull <remote> <local>
			if(CommandInputArray[1].endsWith("pull")){
//				DropboxDownloadFile();
				return true;
			}
			if(CommandInputArray[1].endsWith("push")){
				DropboxUploadFile(client,CommandInputArray[2],CommandInputArray[3]);
				DropboxUploadString(client, CommandInput, FILECMD);
				DropboxWaitNewVerstion(client, FILECMD);
				Info("push file done");
				return true;
			}
		}
		if(DEFINE_LOCAL.length() < CommandInput.length()){
			String shell = CommandInput.substring(DEFINE_LOCAL.length()+1, CommandInput.length());
			Runtime r = Runtime.getRuntime();
			Process p;
			try {
				p = r.exec(shell);
				p.waitFor();
				BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				while ((line = b.readLine()) != null) {
					Info(line);
				}
				b.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			
		}
		
		return true;
	}

	public static String ReadFile(String file) {
		String linetotal = "";
		try {
			File file2 = new File(file);
			FileReader fileReader = new FileReader(file2);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int oneline = 0;
			while ((line = bufferedReader.readLine()) != null) {
				// stringBuffer.append(line);
				// stringBuffer.append("\n");
				linetotal = linetotal + line ;
				if(oneline != 0){
					linetotal = linetotal +"\n";
				}
				oneline++;
			}
			fileReader.close();
			// System.out.println("Contents of file:");
			// System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return linetotal;
	}

	public static void WriteFile(String FileName, String text) {
		try {
			File file = new File(FileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(text);
			bw.close();
		} catch (IOException e) {
//			e.printStackTrace();
		}
	}

	public static void Debug(String Str) {
		System.out.println(Str);
	}

	public static void Info(String Str) {
		System.out.println(Str);
	}
}
