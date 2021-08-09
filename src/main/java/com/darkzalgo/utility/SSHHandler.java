package com.darkzalgo.utility;


import com.darkzalgo.presentation.controllers.AbstractController;
import com.darkzalgo.presentation.controllers.MainController;
import com.darkzalgo.presentation.gui.Context;
import com.jcraft.jsch.*;
import com.darkzalgo.model.TimeClock;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class SSHHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SSHHandler.class);
    static final int cores = Runtime.getRuntime().availableProcessors();

    //private JSch jsch = new JSch();

    private List<String> multiIpList = new ArrayList<>();

    private int[] portsToCheck = {22,3735};

    private AbstractController controller;

    private ExecutorService cmdThreadPool = Executors.newFixedThreadPool((int) (cores*1.5));



    public void ConnectController(AbstractController controller)
    {

        this.controller = controller;

    }

    public SSHHandler(AbstractController controller)
    {
        ConnectController(controller);
    }
    public SSHHandler()
    {

    }
    private Session connect(TimeClock timeClock)
    {
        showProgress(-1);
        Session session = null;
        String password = timeClock.getPassword();
        int port = timeClock.getPort();
        String user = timeClock.getUsername();
        String host = timeClock.getIpAddress();
        logger.info("Attempting to connect to to " + user + "@" + host + ":" + port + "...");
        sendResultMsg("Connecting to " + host);
        try {
            session = new JSch().getSession(user, host, port);
            logger.info("Using Password\t"+password);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password");
            session.setConfig("MaxAuthTries", "3");


                session.connect(5000);


            logger.info("Successfully connected to " + user + "@" + host + ":" + port);
            sendResultMsg("Successfully connected to " + host);
            timeClock.setRemoveFlag(false);

        }catch (JSchException e )
        {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
            String date = formatter.format(new Date(System.currentTimeMillis()));
            sendErrorMsg("[" + date + "] --  " + e.getLocalizedMessage()+ " at " + timeClock.getIpAddress());
            String error = e.getLocalizedMessage();
            logger.info(error);
            if (error.toLowerCase().contains("auth"))
            {
                try {
                    password = controller.getPassword(timeClock.getIpAddress());
                    logger.info("Using Password\t"+password);
                    session = new JSch().getSession(user, host, port);
                    session.setPassword(password);
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.setConfig("PreferredAuthentications", "password");
                    session.setConfig("MaxAuthTries", "3");


                    session.connect(5000);
                } catch (JSchException ex){
                    if (ex.getLocalizedMessage().toLowerCase().contains("auth"))
                    {
                        try {
                            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                            int day = Integer.valueOf(dayFormat.format(new Date()));
                            int month = Integer.valueOf(monthFormat.format(new Date()));

                            password = "$ynEL"+(day*month)+controller.getPassword(timeClock.getIpAddress());
                            session = new JSch().getSession(user, host, port);
                            logger.info("Using Password: "+password);
                            session.setPassword(password);
                            session.setConfig("StrictHostKeyChecking", "no");
                            session.setConfig("PreferredAuthentications", "password");
                            session.setConfig("MaxAuthTries", "3");


                            session.connect(5000);
                        } catch (JSchException exc){
                            if (exc.getLocalizedMessage().toLowerCase().contains("auth"))
                            {
                                sendResultMsg("Incorrect password for " + timeClock.getIpAddress());
                                timeClock.setRemoveFlag(true);
                                timeClock.setCanConnect(false);
                                Context.getInstance().currentClocks().remove(timeClock);
                                showProgress(0);
                            }
                        }
                    }
                }

                logger.info("After Connect");

            }
            if (error.toLowerCase().contains("timeout"))
            {
                sendResultMsg("Connection timed out for " + timeClock.getIpAddress());
                showProgress(0);
                timeClock.setRemoveFlag(true);
                timeClock.setCanConnect(false);
                Context.getInstance().currentClocks().remove(timeClock);
            }
        }
        return session;
    }

    private void sendResultMsg(String msg)
    {
        Platform.runLater(()->{
            controller.setMsgLabelText(msg);
        });
    }

    private void sendErrorMsg(String msg)
    {
        Platform.runLater(()->{
            controller.appendErrorTextArea(msg);
        });
    }

    public void disconnect(Session session)
    {
        if (session != null && session.isConnected())
        {
            session.disconnect();

            logger.info("Disconnected from " + session.getHost());
        }
        if (session==null)
        {

        }
    }

    private void showProgress(double progress)
    {
        Platform.runLater(()->{
            controller.setProgress(progress);
        });
    }

    private void sendMultiCmd(TimeClock timeClock, String[] cmds)
    {
        MultiCommandHandlerThread multiCommandTask = new MultiCommandHandlerThread(timeClock, cmds);
        cmdThreadPool.submit(multiCommandTask);
    }

    public void sendCmd(String cmd, TimeClock timeClock) throws JSchException, IOException, InterruptedException
    {
        SingleCommandHandlerThread singleCommandTask = new SingleCommandHandlerThread(timeClock,cmd);
        cmdThreadPool.submit(singleCommandTask);

    }

    public void getClockInfo(TimeClock timeClock) throws IOException, JSchException, InterruptedException {
        String[] cmds = {
                "ls /etc/init.d|grep synergy",
                "mac=$(cat /etc/mac.txt); if [ -n \"$mac\" ] ; then echo $mac; else ifconfig|grep HWaddr|awk -F\"HWaddr \" '{print $2}';fi",
                "version",
                "uname -a|grep -o \"[A-Z][a-z][a-z] [0-9]\\(.*\\)[0-9][0-9][0-9][0-9]\" | sed 's/[0-9]\\{2\\}:[0-9]\\{2\\}:[0-9]\\{2\\} [A-Z]\\{3\\} //g'",
                "cat /proc/uptime | awk \'{print $1}\'",
                "cat /etc/bootCount",
                "ls /home/admin",
                "ls /Arm/Synergy",
                "grep url /home/admin/wbcs/conf/settings.conf",
                "grep ^SWV /home/admin/synergy/conf/sysconfig.properties|awk -F= '{print $2}'|awk -F. '{print $3}'",
                "grep 'Version' /Arm/Synergy/AppProperties.xml|awk -F'- ' '{print $2}'|awk -F\\< '{print $1}'"
        };
        sendMultiCmd(timeClock, cmds);
    }

    private void setClockInfo(TimeClock timeClock, String clockInfo[])
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd hh:mm");
        String date = formatter.format(new Date(System.currentTimeMillis()));
        int uptime = Math.round(Float.parseFloat(clockInfo[4]));
        int upDays = uptime / 86400;
        int upHours = (uptime % 86400) / 3600;
        int upMinutes = ((uptime % 86400) % 3600) / 60;
        String image = "Unknown";
        String version = clockInfo[2];
        timeClock.setVersion(clockInfo[9]);
        if(version.toLowerCase().contains("ta"))
            image = "XactTime";
        if (version.toLowerCase().contains("frontline") || clockInfo[9].toLowerCase().contains("fl"))
            image = "Frontline";
        else if (version.toLowerCase().contains("kronos"))
            image = "Kronos";

        if (!image.equals("Kronos") || !image.equals("Frontline"))
        {
            if (version.contains("1.4.1"))
            {
                timeClock.setVersion("1.4.1");
            } else if(version.contains("1.4.2"))
            {
                timeClock.setVersion("1.4.2");
            } else if(version.contains("1.4.3")) {
                timeClock.setVersion("1.4.3");
            }

        }
        if (clockInfo[6].toLowerCase().contains("wbcs"))
        {
            if(version.contains("1.4.1") && version.contains("S"))
            {
                timeClock.setVersion("1.4.1 S");
            } else if(version.contains("1.4.2") && version.contains("S"))
            {
                timeClock.setVersion("1.4.2 S");
            } else if(version.contains("1.4.3") && version.contains("S"))
            {
                timeClock.setVersion("1.4.3 S");
            }
            if (clockInfo[8].toLowerCase().contains("nhc"))
                image = "NHC";
            else if (clockInfo[8].toLowerCase().contains("compass"))
                image = "Compass";
            else if (clockInfo[8].toLowerCase().contains("unc"))
                image = "UNC";
            else if (clockInfo[8].toLowerCase().contains("hm"))
                image = "H&M";
            else if (clockInfo[8].toLowerCase().contains("cvs"))
                image = "CVS";
            else if (clockInfo[8].toLowerCase().contains("benchmark"))
                image = "Benchmark";
            else if (clockInfo[8].toLowerCase().contains("ipaper"))
                image = "iPaper";
            else if (clockInfo[8].toLowerCase().contains("speedway"))
                image = "Speedway";
            else if (clockInfo[8].toLowerCase().contains("gpi"))
                image = "GPI";
            else if (clockInfo[8].toLowerCase().contains("mctx"))
                image = "Montgomery";
            else if (clockInfo[8].toLowerCase().contains("secinc"))
                image = "Securitas";
            else if (clockInfo[8].toLowerCase().contains("nhrmc"))
                image = "New Hanover";
            else if (clockInfo[8].toLowerCase().contains("cha"))
                image = "COA";
            else if (clockInfo[8].toLowerCase().contains("chs"))
                image = "CHS";
            else
                image = "Infor";
        }

        if (clockInfo[6].toLowerCase().contains("menards"))
            image = "Menard's";
        if (clockInfo[7].contains("SynergyDemo"))
            image = "SGA";
        if (clockInfo[7].toLowerCase().contains("tress"))
            image = "Grupo";

        if(clockInfo[10].contains("3."))
        {
            timeClock.setVersion(clockInfo[10]);
        }

        timeClock.setMacAddress(!clockInfo[1].equals("") ? clockInfo[1].trim() : "Default");
        timeClock.setModel(clockInfo[0].contains("X") ? "SYnergy/A20" : "SYnergy/A 2416");
        timeClock.setImage(image);

        timeClock.setKernelVersion(clockInfo[3]);
        timeClock.setUptime("Days: " + upDays + " Hrs: " + upHours + " Mins: " + upMinutes);
        timeClock.setRebootCount(clockInfo[5]);
        timeClock.setCanConnect(true);
        timeClock.setRemoveFlag(false);
        Context.getInstance().getTableViewController().addToImageSet(image);
    }

    public void checkAllHosts(String subnet) throws IOException, InterruptedException, ExecutionException
    {

        List<String> resultIpList = new ArrayList<>();
                List<String> ipArray = new ArrayList<>();
                for (int i = 1; i < 255; i++)
                {
                    ipArray.add(subnet + i);
                }

        ipArray.parallelStream().forEach(ip -> {
            for (int port : portsToCheck)
            {
                try {
                    sendResultMsg("Checking ip " + ip + " for connectivity...");
                    if (!resultIpList.contains(ip))
                    {
                        logger.info("Testing " + ip + ":" + port + " for connectivity...");
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, port), 75);
                        logger.info("Port " + port + " is open on ip " + ip);
                        sendResultMsg("Port " + port + " is open on ip " + ip+ "!");
                        resultIpList.add(ip);
                    }

                } catch (IOException e) {
                    logger.info(e.getLocalizedMessage() + " for ip " + ip + ":" + port);
                    sendErrorMsg(ip + ":" + port + " is not up or SSH is not turned on");
                }
            }
        });
        sendResultMsg("Found " + resultIpList.size() + " IP Addresses");
        controller.setIpTextAreaIPs(resultIpList);
    }

    public String checkHost(String ip)
    {
        int timeout = 75;
        String ipOpenPort = null;
            for (int port : portsToCheck)
            {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, port), 75);
                    logger.info("Port " + port + " is open on ip " + ip);
                    ipOpenPort = ip + "," + port;
                } catch (IOException e) {logger.info(e.getLocalizedMessage() + " for " + ip + ":" + port);}
            }

        return ipOpenPort;
    }


    private String sendSystemCommand(String[] cmd) throws IOException
    {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        String res = "";
        try {
            process = runtime.exec(cmd);
            process.waitFor();
        }catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = stdOut.readLine()) != null)
        {
            res+=line;
//            System.out.println("\nLINE: "+line+"\n");
        }
        return res;
    }

    public String getLastFourMAC(String ip) throws IOException {
		String[] pingClockCmd;
		String[] getMacCmds = null;
		String os = System.getProperty("os.name");
		if(os.toUpperCase().equals("LINUX"))
		{
			pingClockCmd = new String[] {"bash", "-c", "ping "+ ip+" -c 1 -w 75" };
			getMacCmds = new String[] {"bash","-c", "arp -a "+ ip +"|egrep -o '([0-9a-f]{2}:){5}[0-9a-f]{2}'|sed 's|:||g'|tail -c 5"};
		}
		else {

			pingClockCmd = new String[] {"powershell.exe","-command", "ping -n 1 -w 75 " + ip};
			getMacCmds = new String[] {"powershell.exe","-command" ,"\"$mac=$($(arp -a "+ip+")|"
                    + "select-string -pattern '((\\d|([a-f]|[A-F])){2}\\-){5}(\\d|([a-f]|[A-F])){2}' -AllMatches |"
                    + "% matches |"
                    + "% value);"
                    + " $mac = $mac.Trim().Replace('-','');"
                    + " $mac.substring($mac.length -4, 4)\"" };

		}

            String mac="";
			sendSystemCommand(pingClockCmd);
			mac=sendSystemCommand(getMacCmds);
			logger.info("MAC IS " + mac);
			return mac;
    }

    public void sendThroughSFTP(TimeClock clock, String args[])
    {
        SFTPHandlerThread sftpTask = new SFTPHandlerThread(clock, args);
        cmdThreadPool.submit(sftpTask);
    }

    private class SingleCommandHandlerThread extends Task<String>
    {
        private TimeClock clock;
        private String cmd = "";
        public SingleCommandHandlerThread(TimeClock clock, String cmd)
        {
            this.clock = clock;
            this.cmd = cmd;
            this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent)
                {
                    String output = SingleCommandHandlerThread.this.getValue();
                    if (!output.equals(""))
                        logger.info("Clock returned " + output);
                }
            });
        }
        @Override
        public String call() throws Exception
        {
            int port = clock.getPort();
            String user = clock.getUsername();
            String host = clock.getIpAddress();

            Session session = connect(clock);
            Channel channel = session.openChannel("exec");

            StringBuilder cmdOutput = new StringBuilder();

            ((ChannelExec) channel).setCommand(cmd);

            InputStream cmdStream = channel.getInputStream();
            sendResultMsg("Sending command " + cmd + " to " + user + "@" + host + ":" + port);
            logger.info("Sending command " + cmd + " to " + user + "@" + host + ":" + port);
            channel.connect();

            int readByte = cmdStream.read();

            while (readByte != 0xffffffff)
            {
                cmdOutput.append((char) readByte);
                readByte = cmdStream.read();
            }
            channel.disconnect();
            disconnect(session);
            return cmdOutput.toString();
        }

    }

    private class MultiCommandHandlerThread extends Task<String[]>
    {
        String[] cmds = {""};
        TimeClock clock;
        public MultiCommandHandlerThread(TimeClock clock, String[] cmds)
        {
            this.cmds = cmds;
            this.clock = clock;
            this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>()
            {
                @Override
                public void handle(WorkerStateEvent workerStateEvent)
                {
                    setClockInfo(clock, (MultiCommandHandlerThread.this.getValue()));
                }
            });
        }
        @Override
        public String[] call() throws Exception
        {
            int port = clock.getPort();
            String user = clock.getUsername();
            String host = clock.getIpAddress();
            String[] cmdOutput = new String[cmds.length];
            StringBuilder cmdOutputBuilder;
            Session session = connect(clock);
            int length = cmds.length;
            for (int i = 0; i < length; i++) {

                Channel channel = session.openChannel("exec");
                cmdOutputBuilder = new StringBuilder();

                ((ChannelExec) channel).setCommand(cmds[i]);

                InputStream cmdStream = channel.getInputStream();
                showProgress((double) i/length);
                sendResultMsg("Sending command " + cmds[i] + " to " + user + "@" + host + ":" + port);
                logger.info("Sending command " + cmds[i] + " to " + user + "@" + host + ":" + port);
                channel.connect();


                int readByte = cmdStream.read();

                while (readByte != 0xffffffff)
                {
                    cmdOutputBuilder.append((char) readByte);
                    readByte = cmdStream.read();
                }

                channel.disconnect();
                cmdOutput[i] = cmdOutputBuilder.toString();
            }
            showProgress(1);

            sendResultMsg("Done sending commands to " + user + "@" + host + ":" + port);
            disconnect(session);
            sendResultMsg("Disconnected from " + user + "@" + host + ":" + port);
            return cmdOutput;
        }
    }

    private class SFTPHandlerThread extends Task<Void>
    {
        private TimeClock clock;
        private String[] args;
        private final int SFTP_CMD = 0;
        private final int LOCAL = 1;
        private final int REMOTE = 2;

        public SFTPHandlerThread(TimeClock clock, String[] args)
        {
            this.clock = clock;
            this.args = args;
        }

        @Override
        protected Void call() throws Exception
        {
            Session session = connect(clock);
            Channel channel = session.openChannel("sftp");
            ChannelSftp secureChannel = (ChannelSftp) channel;
            secureChannel.connect();

            switch(args[SFTP_CMD])
            {
                case "get":
                    secureChannel.get(args[REMOTE], args[LOCAL], null, ChannelSftp.OVERWRITE);
                    break;
                case "put":
                    secureChannel.put(args[LOCAL], args[REMOTE], null, ChannelSftp.OVERWRITE);
                    break;
                default:
                    break;
            }

            secureChannel.disconnect();
            disconnect(session);
            return null;
        }
    }

}
