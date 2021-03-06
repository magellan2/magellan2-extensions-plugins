// Created on 26.02.2008
//
package magellan.plugin.allianceplugin.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import magellan.client.Client;
import magellan.client.utils.ErrorWindow;
import magellan.library.utils.Encoding;
import magellan.library.utils.HTTPClient;
import magellan.library.utils.HTTPResult;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;
import magellan.library.utils.logging.Logger;
import magellan.plugin.allianceplugin.Constants;
import magellan.plugin.allianceplugin.data.OdysseyAlliance;
import magellan.plugin.allianceplugin.data.OdysseyMap;
import magellan.plugin.allianceplugin.data.OdysseyMapPart;

/**
 * This class is the connection between the plugin/client and the
 * server. There are serveral methods available.
 * 
 * - connect (retrieves some server informations)
 * - download (loads a report from the server)
 * - upload (saves a report on the server)
 * - merge (loads and saves orders from/on the server)
 * 
 * Some of this methods uses complex communications between client and
 * server - I mean muliple requests. The protocol is based on HTTP.
 *
 * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0
 */
public class OdysseyClient {
  private static final Logger log = Logger.getInstance(OdysseyClient.class);
  private static OdysseyClient _instance;
  private Base64 base64 = null;
  private HTTPClient client = null;
  
  private String serverURL = null;
  private String userMail = null;
  private String userPass = null;
  
  private OdysseyClient() {
    _instance = this;
    base64 = new Base64();
    log.info("Odyssey Client initialized...");
  }
  
  public static synchronized OdysseyClient getInstance() {
    if (_instance == null) _instance = new OdysseyClient();
    return _instance;
  }
  
  /**
   * This method tries to connect to the server. It uses the following informations.
   * 
   * (serverURL)/connect.php?user=usermail&pass=userpass
   */
  public OdysseyServerInformation connect() {
    if (Utils.isEmpty(serverURL) || serverURL.length()<10) return null;
    if (Utils.isEmpty(userMail) || userMail.length()<6) return null;
    if (Utils.isEmpty(userPass) || userPass.length()<6) return null;
    
    try {
      URI uri = new URI(serverURL+"/connect.php?user="+URLEncoder.encode(userMail,Encoding.ISO.toString())+"&pass="+URLEncoder.encode(new String(base64.encode(userPass.getBytes())),Encoding.ISO.toString()));
      
      client = new HTTPClient(Client.INSTANCE.getProperties());
      HTTPResult result = client.get(uri);
      
      if (result == null) return null;
      if (result.getStatus() == 403) {
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        return null;
      }
      if (result.getStatus() != 200) {
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        return null;
      }
      
      String content = result.getResultAsString();
      
      return new OdysseyServerInformation(content);
    } catch (Exception exception) {
      log.error("",exception);
      ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE),exception.getMessage(),exception);
      window.setShutdownOnCancel(false);
      window.setVisible(true);
    }
    return null;
  }

  /**
   * This method loads a report from the server and informs the observer about the
   * download progress.
   */
  public void getMap(Observer observer, OdysseyAlliance alliance, OdysseyMap map, OdysseyMapPart mappart) {
    try {
      String user = URLEncoder.encode(userMail,Encoding.ISO.toString());
      String pass = URLEncoder.encode(new String(base64.encode(userPass.getBytes())),Encoding.ISO.toString());
      String mapname = URLEncoder.encode(mappart.getName(),Encoding.ISO.toString());
      int version = mappart.getVersion();
      URI uri = new URI(serverURL+"/download.php?user="+user+"&pass="+pass+"&map="+mapname+"&version="+version);
      
      log.info("Download Map "+mappart.getName());

      client = new HTTPClient(Client.INSTANCE.getProperties());
      HTTPResult result = client.get(uri,true);
      
      if (result == null) {
        log.error("HTTP client returns no result");
        observer.setFile(null);
        return;
      }
      if (result.getStatus() == 403) {
        log.error("Authentification required");
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        observer.setFile(null);
        return;
      }
      if (result.getStatus() != 200) {
        log.error("HTTP server response is "+result.getStatus());
        observer.setFile(null);
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        observer.setFile(null);
        return;
      }
      
      InputStream stream = result.getStream();
      log.info("Downloading file");
      
      File tmpFile = File.createTempFile("map_"+version, ".cr.bz2");
      tmpFile.deleteOnExit();
      FileOutputStream fos = new FileOutputStream(tmpFile);
      byte[] buffer = new byte[4096];
      long size = 0l;
      
      while (true) {
        int read = stream.read(buffer);
        if (read < 0) break;
        size+=read;
        fos.write(buffer,0,read);
        
        observer.transfer(size);
      }
      log.info("File saved in "+tmpFile);
      
      fos.close();
      stream.close();
      
      observer.setFile(tmpFile);
      
    } catch (Exception exception) {
      log.error("",exception);
      ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE),exception.getMessage(),exception);
      window.setShutdownOnCancel(false);
      window.setVisible(true);
      observer.setFile(null);
    }
  }
  
  /**
   * This method tries to send the given file to the odyssey server. 
   * Then there runs a merge process.
   */
  public void sendMap(Observer observer, OdysseyAlliance alliance, OdysseyMap map, OdysseyMapPart mappart, File crFile) {
    try {
      String user = URLEncoder.encode(userMail,Encoding.ISO.toString());
      String pass = URLEncoder.encode(new String(base64.encode(userPass.getBytes())),Encoding.ISO.toString());
      String mapname = URLEncoder.encode(mappart.getName(),Encoding.ISO.toString());
      int version = mappart.getVersion();
      URI uri = new URI(serverURL+"/upload.php");
      
      log.info("Upload Map "+crFile.getName());
      
      client = new HTTPClient(Client.INSTANCE.getProperties());
      List<Part> parts = new ArrayList<Part>();
      parts.add(new StringPart("user",user));
      parts.add(new StringPart("pass",pass));
      parts.add(new StringPart("map",mapname));
      parts.add(new StringPart("partId",String.valueOf(mappart.getId())));
      parts.add(new StringPart("version",String.valueOf(version)));
      parts.add(new StringPart("round",String.valueOf(Client.INSTANCE.getData().getDate().getDate())));
      parts.add(new FilePart("crfile",new ObservableFilePartSource(observer,crFile,true)));
      HTTPResult result = client.post(uri,parts.toArray( new Part[parts.size()]));
      
      if (result == null) {
        log.error("HTTP client returns no result");
        observer.setFile(null);
        return;
      }
      if (result.getStatus() == 403) {
        log.error("Authentification required");
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        observer.setFile(null);
        return;
      }
      if (result.getStatus() != 200) {
        log.error("HTTP server response is "+result.getStatus());
        observer.setFile(null);
        ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE)+"\n"+result.getResultAsString(),"URL:"+uri,null);
        window.setShutdownOnCancel(false);
        window.setVisible(true);
        observer.setFile(null);
        return;
      }
      
      log.info("Result: "+result.getResultAsString());
      
      // we are ready...
      observer.transfer(crFile.length());
      observer.setFile(crFile);

      
    } catch (Exception exception) {
      log.error("",exception);
      ErrorWindow window = new ErrorWindow(Client.INSTANCE,Resources.get(Constants.RESOURCE_SERVER_CONNECT_FAILED_MESSAGE),exception.getMessage(),exception);
      window.setShutdownOnCancel(false);
      window.setVisible(true);
      observer.setFile(null);
    }
  }

  
  /**
   * This method returns the field serverURL
   *
   * @return the serverURL
   */
  public String getServerURL() {
    return serverURL;
  }

  /**
   * This method sets the field serverURL to serverURL
   *
   * @param serverURL the serverURL to set
   */
  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  /**
   * This method returns the field userMail
   *
   * @return the userMail
   */
  public String getUserMail() {
    return userMail;
  }

  /**
   * This method sets the field userMail to userMail
   *
   * @param userMail the userMail to set
   */
  public void setUserMail(String userMail) {
    this.userMail = userMail;
  }

  /**
   * This method returns the field userPass
   *
   * @return the userPass
   */
  public String getUserPass() {
    return userPass;
  }

  /**
   * This method sets the field userPass to userPass
   *
   * @param userPass the userPass to set
   */
  public void setUserPass(String userPass) {
    this.userPass = userPass;
  }

  /**
   * This class overrides the FilePartSource and makes it possible
   * to load a file and inform an observer.
   *
   * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
   * @version 1.0
   */  
  class ObservableFilePartSource extends FilePartSource {
    private Observer observer = null;
    private File file = null;
    private boolean dontReportLastByte = false;
    
    public ObservableFilePartSource(Observer observer, File file, boolean dontReportLastByte) throws FileNotFoundException {
      super(file);
      this.file = file;
      this.observer = observer;
      this.dontReportLastByte = dontReportLastByte;
    }
    
    /**
     * @see org.apache.commons.httpclient.methods.multipart.FilePartSource#createInputStream()
     */
    @Override
    public InputStream createInputStream() throws FileNotFoundException {
      return new ObservableFileInputStream(observer, file, dontReportLastByte);
    }
    
    /**
     * This class overrides the FileInputStream and makes it possible
     * to load a file and inform an observer.
     *
     * @author <a href="thoralf@m84.de">Thoralf Rickert</a>
     * @version 1.0
     */
    class ObservableFileInputStream extends FileInputStream {
      private Observer observer = null;
      private File file = null;
      private long sum = 0;
      private boolean dontReportLastByte = false;
      
      public ObservableFileInputStream(Observer observer, File file, boolean dontReportLastByte) throws FileNotFoundException  {
        super(file);
        this.observer = observer;
        this.file = file;
        this.dontReportLastByte = dontReportLastByte;
      }
      
      public int read(byte b[], int off, int len) throws IOException {
        int read = super.read(b,off,len);
        informObserver(read);
        return read;
      }

      /**
       * @see java.io.FileInputStream#read()
       */
      @Override
      public int read() throws IOException {
        int read = super.read();
        informObserver(1);
        return read;
      }

      /**
       * @see java.io.FileInputStream#read(byte[])
       */
      @Override
      public int read(byte[] b) throws IOException {
        int read = super.read(b);
        informObserver(read);
        return read;
      }
      
      private void informObserver(int read) {
        sum += read;
        
        if (sum == file.length() && dontReportLastByte) {
          sum--;
        }
        observer.transfer(sum);
      }
      
    }
  }
}
