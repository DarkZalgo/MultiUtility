package com.darkzalgo.utility;


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
import java.io.IOException;
import java.io.InputStream;
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

    private JSch jsch = new JSch();

    private List<String> multiIpList = new ArrayList<>();

    private int[] portsToCheck = {22,3735};

    private static MainController controller;



    public static void ConnectController(MainController controller)
    {

        SSHHandler.controller = controller;

    }

    public SSHHandler(MainController controller)
    {
        ConnectController(controller);
    }
    public SSHHandler()
    {

    }
    private Session connect(TimeClock timeClock)
    {
        Session session = null;
        String password = timeClock.getPassword();
        int port = timeClock.getPort();
        String user = timeClock.getUsername();
        String host = timeClock.getIpAddress();
        logger.info("Attempting to connect to to " + user + "@" + host + ":" + port + "...");
        setMainLabel("Attempting to connect to to " + user + "@" + host + ":" + port + "...");
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password");
            session.setConfig("MaxAuthTries", "1");

            synchronized (this)
            {
                session.connect(5000);
            }

            logger.info("Successfully connected to " + user + "@" + host + ":" + port);
            setMainLabel("Successfully connected to " + user + "@" + host + ":" + port);
            timeClock.setRemoveFlag(false);

        }catch (JSchException e )
        {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
            String date = formatter.format(new Date(System.currentTimeMillis()));
            appendMainErrorArea("[" + date + "] --  " + e.getLocalizedMessage()+ " at " + timeClock.getIpAddress());
            /*logger.info("Could not connect to " + user + "@" + host + ":" + port);
            sendLabel("Could not connect to " + user + "@" + host + ":" + port);*/
            String error = e.getLocalizedMessage();
            logger.info(error);
            if (error.toLowerCase().contains("auth"))
            {
                setMainLabel("Authorization Failure at " + timeClock.getIpAddress());
                timeClock.setRemoveFlag(true);
                timeClock.setCanConnect(false);
                Context.getInstance().currentClocks().remove(Context.getInstance().currentClocks().indexOf(timeClock));
            }
        }
        return session;
    }

    private void setMainLabel(String msg)
    {
        Platform.runLater(()->{
            controller.setMsgLabelText(msg);
        });
    }

    private void appendMainErrorArea(String msg)
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

    private void sendMultiCmd(TimeClock timeClock, String[] cmds) {
        Task task = new Task<String[]>()
        {

            @Override
            public String[] call() throws Exception
            {

            int port = timeClock.getPort();
            String user = timeClock.getUsername();
            String host = timeClock.getIpAddress();
            String[] cmdOutput = new String[cmds.length];
            StringBuilder cmdOutputBuilder;
            Session session = connect(timeClock);

            for (int i = 0; i < cmds.length; i++) {
                Channel channel = session.openChannel("exec");
                cmdOutputBuilder = new StringBuilder();

                ((ChannelExec) channel).setCommand(cmds[i]);

                InputStream cmdStream = channel.getInputStream();
                setMainLabel("Sending command \n" + cmds[i] + " \nto " + user + "@" + host + ":" + port);
                logger.info("Sending command \n" + cmds[i] + " \nto " + user + "@" + host + ":" + port);
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

            setMainLabel("Done sending commands to " + user + "@" + host + ":" + port);
            disconnect(session);
            setMainLabel("Disconnected from " + user + "@" + host + ":" + port);
            return cmdOutput;
            }
        };

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>()
        {
            @Override
            public void handle(WorkerStateEvent workerStateEvent)
            {
                setClockInfo(timeClock, (String[])task.getValue());
            }
        });
        new Thread(task).start();
    }

    public void sendCmd(String cmd, TimeClock timeClock) throws JSchException, IOException, InterruptedException {
        Task task = new Task<String>()
        {
            @Override
            public String call() throws Exception
            {
                int port = timeClock.getPort();
                String user = timeClock.getUsername();
                String host = timeClock.getIpAddress();

                Session session = connect(timeClock);
                Channel channel = session.openChannel("exec");

                StringBuilder cmdOutput = new StringBuilder();

                ((ChannelExec) channel).setCommand(cmd);

                InputStream cmdStream = channel.getInputStream();
                setMainLabel("Sending command \n" + cmd + " \nto " + user + "@" + host + ":" + port);
                logger.info("Sending command \n" + cmd + " \nto " + user + "@" + host + ":" + port);
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
        };
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent)
            {
                String output = (String) task.getValue();
                if (!output.equals(""))
                logger.info("Clock returned " + output);
            }
        });
       Thread taskThread = new Thread(task);
       taskThread.start();

    }

    public void getClockInfo(TimeClock timeClock) throws IOException, JSchException, InterruptedException {
        String[] cmds = {
                "ls /etc/init.d|grep synergy",
                "cat /etc/mac.txt",
                "version",
                "uname -a|grep -o \"[A-Z][a-z][a-z] [0-9]\\(.*\\)[0-9][0-9][0-9][0-9]\" | sed 's/[0-9]\\{2\\}:[0-9]\\{2\\}:[0-9]\\{2\\} [A-Z]\\{3\\} //g'",
                "cat /proc/uptime | awk \'{print $1}\'",
                "cat /etc/bootCount",
                "ls /home/admin",
                "ls /Arm/Synergy",
                "grep url /home/admin/wbcs/conf/settings.conf",
                "grep ^SWV /home/admin/synergy/conf/sysconfig.properties|awk -F= '{print $2}'|awk -F. '{print $3}'"
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

        timeClock.setMacAddress(!clockInfo[1].equals("") ? clockInfo[1] : "Default");
        timeClock.setModel(clockInfo[0].contains("X") ? "SYnergy/A20" : "SYnergy/A 2416");
        timeClock.setImage(image);

        timeClock.setKernelVersion(clockInfo[3]);
        timeClock.setUptime("Days: " + upDays + " Hrs: " + upHours + " Mins: " + upMinutes);
        timeClock.setRebootCount(clockInfo[5]);
        timeClock.setCanConnect(true);
        timeClock.setRemoveFlag(false);
    }

    public void checkAllHosts(String subnet) throws IOException, InterruptedException, ExecutionException {

        final List<String> resultIpList = new ArrayList<>();

        Task task = new Task<List<String>>() {
            @Override
            public List<String> call() throws Exception
            {
                List<String> ipArray = new ArrayList<>();
                for (
                        int i = 1;
                        i < 255; i++) {
                    logger.info("Adding " + subnet + i + " to ipArray");
                    ipArray.add(subnet + i);
                }

                ipArray.parallelStream().forEach((ip -> {
                            for (int port : portsToCheck)
                            {
                                try {
                                    setMainLabel("Checking ip " + ip + " for connectivity...");

                                    if (!resultIpList.contains(ip)) {
                                        logger.info("Testing " + ip + ":" + port + " for connectivity...");
                                        Socket socket = new Socket();
                                        socket.connect(new InetSocketAddress(ip, port), 75);
                                        logger.info("Port " + port + " is open on ip " + ip);
                                        setMainLabel("Port " + port + " is open on ip " + ip+ "!");
                                        if (port == 8080) {
                                            logger.info("Host " + ip + " is up but SSH is not on.");

                                        } else resultIpList.add(ip);
                                    }

                                } catch (IOException e) {
                                    logger.info(e.getLocalizedMessage() + " for ip " + ip + ":" + port);
                                    appendMainErrorArea(ip + ":" + port + " is not up or SSH is not turned on");
                                }
                            }
                        }));
                setMainLabel("Found " + resultIpList.size() + " IP Addresses");
            return resultIpList;
        }
        };

       task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
           @Override
           public void handle(WorkerStateEvent workerStateEvent)
           {
               List<String> res = (List<String>) task.getValue();
               controller.setIpTextAreaIPs(res);
           }
       });
       new Thread(task).start();
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
                } catch (IOException e) {logger.info(e.getLocalizedMessage() + " for ip " + ip + "and port " + port);}
            }

        return ipOpenPort;
    }

    public void sendThroughSFTP()
    {
        Task task = new Task<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                return null;
            }
        };
    }

}
