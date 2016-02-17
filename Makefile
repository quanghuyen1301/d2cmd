all:
	javac    -cp dropbox-core-sdk-1.7.7.jar:jackson-core-2.2.4.jar  d2cmd.java
client:all
	java -cp .:dropbox-core-sdk-1.7.7.jar:jackson-core-2.2.4.jar  d2cmd client
server:all
	java -cp .:dropbox-core-sdk-1.7.7.jar:jackson-core-2.2.4.jar  d2cmd  server
clean:
	rm -rf d2cmd.class filecmd shell	
