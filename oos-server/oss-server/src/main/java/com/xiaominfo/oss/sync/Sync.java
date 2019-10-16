package com.xiaominfo.oss.sync;

import com.xiaominfo.oss.common.SpringContextUtil;
import com.xiaominfo.oss.module.model.OSSMaterialInfo;
import com.xiaominfo.oss.service.OSSInformationService;
import com.xiaominfo.oss.service.OSSMaterialInfoService;
import com.xiaominfo.oss.sync.netty.SalveServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 集群分发
 */
public class Sync {
    private static Logger logger = LoggerFactory.getLogger(Sync.class);
    private static List<Node> nodeList = new ArrayList<>();


    static {
        String[] node = SpringContextUtil.getProperty("material.nodes").split(",");
        for (String str : node) {
            int port = Integer.parseInt(str.split(":")[1]);
            String ip = str.split(":")[0];
            Node n = new Node(port, ip);
            nodeList.add(n);
        }
        startMaster();
    }

    /**
     * 同步到其他节点
     */
    public static void syncSalve(String root, File file, String materialId) {
        for (Node node : nodeList) {
            try {
                Sync.startSalve(root, node, file, materialId);
                //syncMessage(root, node, file, materialId);
            } catch (Exception e) {
                logger.error("node=[{}]同步失败,fileName=[{}]", node, file.getName());
            }
        }
    }

    private static void startSalve(String root, Node node, File file, String materialId) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Runnable runnable = () -> Sync.syncMessage(root, node, file, materialId);
        executorService.submit(runnable);
    }

    /**
     * 发送文件同步
     *
     * @param root
     * @param node
     * @param file
     * @param materialId
     */
    protected static void syncMessage(String root, Node node, File file, String materialId) {
        try {
            SyncMessage uploadFile = new SyncMessage();
            String fileMd5 = file.getName();// 文件名
            uploadFile.setFile(file);
            uploadFile.setFile_md5(fileMd5);
            uploadFile.setFilePath(file.getParent().replaceFirst(root.replaceAll("/", "\\\\").replaceAll("\\\\", "\\\\\\\\"), "") + File.separator);
            uploadFile.setStarPos(0);// 文件开始位置
            uploadFile.setMaterialId(materialId);
            logger.info("开始 node = [{}] 节点同步 file = [{}]...", node, file.getName());
            if (file.exists()) {
                new SalveServer().connect(node.getPort(), node.getIp(), uploadFile);
            } else {
                logger.info("file = [{}] 不存在,不同步", file.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startMaster() {
        String localIp = "";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress().toString();
        } catch (UnknownHostException e) {
            logger.info("未找到本机ip：", e);
        }
        int masterPort = Integer.parseInt(SpringContextUtil.getProperty("material.masterPort"));
        Node node = new Node(masterPort, localIp);


        OSSInformationService ossInformationService = SpringContextUtil.getBean(OSSInformationService.class);
        String root = ossInformationService.queryOne().getRoot();

        ExecutorService executorService = Executors.newFixedThreadPool(nodeList.size());

        for (Node thatNode : nodeList) {
            try {
                logger.info("开启 node[{}] 文件同步线程", thatNode);
                SyncRunnable syncRunnable = new SyncRunnable();
                syncRunnable.setNode(node);
                syncRunnable.setThatNode(thatNode);
                syncRunnable.setRoot(root);
                executorService.submit(syncRunnable);
            } catch (Exception e) {
                logger.error("node=[{}]同步失败", e);
            }
        }
    }

}

class SyncRunnable implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(Sync.class);
    private String root;
    private Node node;
    private Node thatNode;

    @Override
    public void run() {
        while (true) {
            OSSMaterialInfoService syncService = SpringContextUtil.getBean(OSSMaterialInfoService.class);
            List<OSSMaterialInfo> materialInfos = syncService.waitSync(node.toString(), thatNode.toString());

            for (OSSMaterialInfo info : materialInfos) {
                try {
                    File file = new File(root + File.separator + info.getStorePath());
                    //Sync.syncSalve(root, file, info.getId());
                    Sync.syncMessage(root, thatNode, file, info.getId());
                } catch (Exception e) {
                    logger.error("当前文件同步失败 material id =[{}]", info.getId(), e);
                }
            }
            try {
                Thread.sleep(1000 * 60 * 10);//10分钟
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Node getThatNode() {
        return thatNode;
    }

    public void setThatNode(Node thatNode) {
        this.thatNode = thatNode;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }
}
