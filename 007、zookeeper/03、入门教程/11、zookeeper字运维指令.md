

# zookeeper４字运维指令


## stat is not executed because it is not in the whitelist.

- [原文](https://blog.csdn.net/x763795151/article/details/80599498)

１. 问题描述   
本来是想用wchc查看监听路径信息的，结果使用这个指令查询的时候，zookeeper服务器返回的响应信息是：

wchc is not executed because it is not in the whitelist.

２. 分析原因   
查了下日志，找到了这条信息是从这个类org.apache.zookeeper.server.NettyServerCnxn里返回的，
然后直接上github找到源码在这个类里，找到这条信息出现的方法体内，如下：
```java
/** Return if four letter word found and responded to, otw false **/
    private boolean checkFourLetterWord(final Channel channel,
            ChannelBuffer message, final int len) throws IOException
    {
        // We take advantage of the limited size of the length to look
        // for cmds. They are all 4-bytes which fits inside of an int
        if (!FourLetterCommands.isKnown(len)) {
            return false;
        }
 
        String cmd = FourLetterCommands.getCommandString(len);
 
        channel.setInterestOps(0).awaitUninterruptibly();
        packetReceived();
 
        final PrintWriter pwriter = new PrintWriter(
                new BufferedWriter(new SendBufferWriter()));
 
        // ZOOKEEPER-2693: don't execute 4lw if it's not enabled.
        // 根据代码可以看到这个地方是用来判断是否启用这个指令，如果不启用，就会返回这条信息。
        if (!FourLetterCommands.isEnabled(cmd)) {
            LOG.debug("Command {} is not executed because it is not in the whitelist.", cmd);
            NopCommand nopCmd = new NopCommand(pwriter, this, cmd +
                    " is not executed because it is not in the whitelist.");
            nopCmd.start();
            return true;
        }
 
        LOG.info("Processing " + cmd + " command from "
                + channel.getRemoteAddress());
 
       if (len == FourLetterCommands.setTraceMaskCmd) {
            ByteBuffer mask = ByteBuffer.allocate(8);
            message.readBytes(mask);
            mask.flip();
            long traceMask = mask.getLong();
            ZooTrace.setTextTraceLevel(traceMask);
            SetTraceMaskCommand setMask = new SetTraceMaskCommand(pwriter, this, traceMask);
            setMask.start();
            return true;
        } else {
            CommandExecutor commandExecutor = new CommandExecutor();
            return commandExecutor.execute(this, pwriter, len, zkServer,factory);
        }
    }
```
看我中文注释的地方，就可以确定问题点在FourLetterCommands这个类里，
然后进入这个类：org.apache.zookeeper.server.command.FourLetterCommands，找到isEnabled这个方法，源码如下：
```java
 /**
     * Check if the specified command is enabled.
     *
     * In ZOOKEEPER-2693 we introduce a configuration option to only
     * allow a specific set of white listed commands to execute.
     * A command will only be executed if it is also configured
     * in the white list.
     *
     * @param command The command string.
     * @return true if the specified command is enabled
     */
    public synchronized static boolean isEnabled(String command) {
        // whiteListInitialized这个值不用关心，它就是是否初始化过的标记，默认是false，第一次进入这个方法体后，whiteListedCommands相关命令加载完成后，就认为初始化完成了，这个值就为true了
        if (whiteListInitialized) {
            return whiteListedCommands.contains(command);
        }
 
        // 根据下面这几行代码，便可以看出，这些４字指令，是配置在ＶＭ变量内的，而key值是ZOOKEEPER_4LW_COMMANDS_WHITELIST，其实它的常量定义在最上面，这个常量的值为：zookeeper.4lw.commands.whitelist
        //看下面的代码便知道，４字指令的格式是用逗号（,）分隔，也可以直接用*，则会把cmd2String里已经缓存的所有指令，迭代的添加到whiteListedCommands这个白名单里
        String commands = System.getProperty(ZOOKEEPER_4LW_COMMANDS_WHITELIST);
        if (commands != null) {
            String[] list = commands.split(",");
            for (String cmd : list) {
                if (cmd.trim().equals("*")) {
                    for (Map.Entry<Integer, String> entry : cmd2String.entrySet()) {
                        whiteListedCommands.add(entry.getValue());
                    }
                    break;
                }
                if (!cmd.trim().isEmpty()) {
                    whiteListedCommands.add(cmd.trim());
                }
            }
        }
 
        // It is sad that isro and srvr are used by ZooKeeper itself. Need fix this
        // before deprecating 4lw.
        if (System.getProperty("readonlymode.enabled", "false").equals("true")) {
            whiteListedCommands.add("isro");
        }
        // zkServer.sh depends on "srvr".
        whiteListedCommands.add("srvr");
        whiteListInitialized = true;
        LOG.info("The list of known four letter word commands is : {}", Arrays.asList(cmd2String));
        LOG.info("The list of enabled four letter word commands is : {}", Arrays.asList(whiteListedCommands));
        return whiteListedCommands.contains(command);
    }
```
下面是cmd2String初始化缓存的所有指令，结合上面的代码来看：
```java
    // specify all of the commands that are available
    static {
        cmd2String.put(confCmd, "conf");
        cmd2String.put(consCmd, "cons");
        cmd2String.put(crstCmd, "crst");
        cmd2String.put(dirsCmd, "dirs");
        cmd2String.put(dumpCmd, "dump");
        cmd2String.put(enviCmd, "envi");
        cmd2String.put(getTraceMaskCmd, "gtmk");
        cmd2String.put(ruokCmd, "ruok");
        cmd2String.put(setTraceMaskCmd, "stmk");
        cmd2String.put(srstCmd, "srst");
        cmd2String.put(srvrCmd, "srvr");
        cmd2String.put(statCmd, "stat");
        cmd2String.put(wchcCmd, "wchc");
        cmd2String.put(wchpCmd, "wchp");
        cmd2String.put(wchsCmd, "wchs");
        cmd2String.put(mntrCmd, "mntr");
        cmd2String.put(isroCmd, "isro");
        cmd2String.put(telnetCloseCmd, "telnet close");
    }
```
重点看我上面代码的中文说明，便明白，可以在启动脚本里添加ＶＭ环境变量-Dzookeeper.4lw.commands.whitelist=*，
便可以把所有指令添加到白名单，我是添加在脚本的这个位置：
```
    ZOOMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMXPORT 
-Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL 
-Dzookeeper.jmx.log4j.disable=$JMXLOG4J org.apache.zookeeper.server.quorum.QuorumPeerMain"
  fi
else
    echo "JMX disabled by user request" >&2
    ZOOMAIN="org.apache.zookeeper.server.quorum.QuorumPeerMain"
fi
# 这里就是我添加的
# 如果不想添加在这里，注意位置和赋值的顺序
ZOOMAIN="-Dzookeeper.4lw.commands.whitelist=* ${ZOOMAIN}"
```

 
 


