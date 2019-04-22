package com.qq.view;

import com.qq.model.entity.FriendGroup;
import com.qq.model.entity.FriendGroupUser;
import com.qq.model.entity.Group;
import com.qq.model.entity.User;
import com.qq.model.service.ServiceFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

/**
 * @author fsc
 * 功能：构建用户主界面，可以跳转到聊天窗口，也可以跳转到设置界面。并且传入socket参数。
 * 属性：user――该登录后用户的实例对象	path――JTree好友列表里面最高层好友昵称	count――JTree选中的层级
 * groupPath――群列表底下的群昵称	groupCount――JTree选中的层级		color――主界面的颜色
 */
public class UserView extends JFrame {

    private static final long serialVersionUID = 1L;
    private int color = 0xffffff;
    private static String friendBtnNormal = System.getProperty("user.dir") + "\\src\\imgs\\friendnormal.png";
    private static String friendBtnClick = System.getProperty("user.dir") + "\\src\\imgs\\friendclick.png";
    private static String textBtnNormal = System.getProperty("user.dir") + "\\src\\imgs\\textnormal.png";
    private static String textBtnClick = System.getProperty("user.dir") + "\\src\\imgs\\textclick.png";
    private static String groupBtnNormal = System.getProperty("user.dir") + "\\src\\imgs\\groupnormal.png";
    private static String groupBtnClick = System.getProperty("user.dir") + "\\src\\imgs\\groupclick.png";
    private final Socket client;
    private User user;
    private static String path;
    private static int count;
    private static String groupPath;
    private static int groupCount;

    public UserView(String name, Socket client) {
        super(name);
        this.client = client;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        UserView.createUserView("test", 0xffffff, new Socket("127.0.0.1", 1995), "111111");
    }

    public static UserView createUserView(String name, int color, Socket client, String account) {
        System.out.println("UserView:" + Thread.currentThread().getName());
        UserView uv = new UserView(name, client);
        uv.user = (User) ServiceFactory.getService("user").findByCondition(account);
        uv.setColor(color);
        JPanel jp = (JPanel) uv.getContentPane();
        jp.setBackground(new Color(uv.getColor()));
        jp.setLayout(null);
        JLabel jlicon = createIconLabel(10, 20, 78, 78, uv);
        JPanel jluserinfo = createUserInfo(uv.getColor(), uv);
        JTextField jtfsearch = createSearchInput(uv.getColor());
        JPanel jplist = createListPanel(uv.getColor(), uv);
        JPanel jpbottom = createBottom(uv);
        jp.add(jtfsearch);
        jp.add(jlicon);
        jp.add(jluserinfo);
        jp.add(jplist);
        jp.add(jpbottom);

        uv.setBounds(1400, 180, 400, 723);
        uv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        uv.setResizable(false);
        uv.setVisible(true);
        return uv;

    }

    /**
     * 底部设置部分
     * @param jf
     * @return
     */
    private static JPanel createBottom(final UserView jf) {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        jp.setVisible(true);
        jp.setOpaque(false);
        jp.setBounds(0, 650, 400, 100);

        final JButton jbset = new JButton("设置");
        jbset.setBounds(1, 0, 133, 40);
        jbset.setBorderPainted(false);
        jbset.setFont(new Font("宋体", Font.ITALIC, 14));
        jbset.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        SetView.createSetView("Set", "set", jf.user.getAccount());
                    }
                }).start();
            }
        });

        final JButton jbsearch = new JButton("管理好友/群");
        jbsearch.setBounds(135, 0, 133, 40);
        jbsearch.setBorderPainted(false);
        jbsearch.setFont(new Font("宋体", Font.ITALIC, 14));
        jbsearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
//						此处进入到设置界面，并传入用户参数
                        SetView.createSetView("Set", "search", jf.user.getAccount());
                    }
                }).start();
            }
        });

        JButton jbexit = new JButton("退出登录");
        jbexit.setBounds(269, 0, 133, 40);
        jbexit.setForeground(Color.green);
        jbexit.setBorderPainted(false);
        jbexit.setFont(new Font("宋体", Font.ITALIC, 14));
        jbexit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        jp.add(jbset);
        jp.add(jbsearch);
        jp.add(jbexit);
        return jp;
    }

    /**
     * 用户列表按钮部分
     * @param bgcolor
     * @param uv
     * @return
     */
    private static JPanel createListPanel(int bgcolor, UserView uv) {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        JButton jb1 = null, jb2 = null, jb3 = null;
        JPanel jp1 = null, jp2 = null, jp3 = null;
        JPanel[] jpArr = new JPanel[3];
        jb1 = createFriendBtn(0, bgcolor);
        jb2 = createFriendBtn(132, bgcolor);
        jb3 = createFriendBtn(264, bgcolor);
        jp1 = createFriendList(bgcolor, uv);
        jp2 = createGroupList(bgcolor, uv);
        jp3 = createTextList(bgcolor, uv);
        jpArr[0] = jp1;
        jpArr[1] = jp2;
        jpArr[2] = jp3;
        addClickEvent(jb1, jp1, jpArr, friendBtnClick, friendBtnClick, jb1, jb2, jb3);
        addClickEvent(jb2, jp2, jpArr, groupBtnNormal, groupBtnClick, jb1, jb2, jb3);
        addClickEvent(jb3, jp3, jpArr, textBtnNormal, textBtnClick, jb1, jb2, jb3);
        jp.add(jb1);
        jp.add(jb2);
        jp.add(jb3);
        jp.add(jp1);
        jp.add(jp2);
        jp.add(jp3);
        jp.setBounds(0, 150, 400, 500);
        jp.setVisible(true);
        jp.setBackground(new Color(bgcolor));
        return jp;
    }

    public static JButton createFriendBtn(int marginLeft, int bgcolor) {
        final JButton jb = new JButton();
        jb.setBounds(marginLeft, 0, 133, 40);
        jb.setBorder(new MyBorder(bgcolor));
        return jb;
    }

    /**
     * 用户列表
     * @param bgcolor
     * @param uv
     * @return
     */
    @SuppressWarnings("unchecked")
    private static JPanel createFriendList(int bgcolor, final UserView uv) {
        JPanel jp = new JPanel();
        JTree tree = null;
        jp.setLayout(null);
        DefaultMutableTreeNode group = new DefaultMutableTreeNode("好友分组");
//		DefaultMutableTreeNode group1 = new DefaultMutableTreeNode("我的好友");
//		DefaultMutableTreeNode group2 = new DefaultMutableTreeNode("朋友");
//		DefaultMutableTreeNode group3 = new DefaultMutableTreeNode("同学");
//		DefaultMutableTreeNode group1_item1 = new DefaultMutableTreeNode("admin");
//		DefaultMutableTreeNode group2_item1 = new DefaultMutableTreeNode("admin");
//		DefaultMutableTreeNode group3_item1 = new DefaultMutableTreeNode("admin");
//		group1.add(group1_item1);
//		group2.add(group2_item1);
//		group3.add(group3_item1);
//		group.add(group1);
//		group.add(group2);
//		group.add(group3);
        int id = uv.user.getId();
        List<FriendGroupUser> list = ServiceFactory.getService("friendGroupUser").listPart(String.valueOf(id));
        for (int i = 0; i < list.size(); i++) {
            FriendGroup fg = (FriendGroup) ServiceFactory.getService("friendGroup").findById(list.get(i).getFriendGroupId());
            System.out.println("加载好友:" + fg.getName().split(" ")[1]);
            if (fg.getName().split(" ")[1].equals(uv.user.getAccount())) {
                DefaultMutableTreeNode newGroup = new DefaultMutableTreeNode(fg.getName().split(" ")[0]);
                group.add(newGroup);
                List<FriendGroupUser> friend = ServiceFactory.getService("friendGroupUser").listPart("1", String.valueOf(fg.getId()));
                for (int j = 0; j < friend.size(); j++) {
                    User fri = (User) ServiceFactory.getService("user").findById(friend.get(j).getUserId());
                    if (fri.getAccount().equals(uv.user.getAccount())) {
//						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("本人");
//						newGroup.add(newNode);
                        continue;
                    } else {
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(fri.getName() + "(" + fri.getAccount() + ")");
                        newGroup.add(newNode);
                    }
                }
            }
        }
        tree = new JTree(group);
        tree.setBounds(20, 10, 400, 450);
        tree.setFont(new Font("宋体", Font.BOLD, 15));
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                path = e.getPath().getLastPathComponent().toString();
                count = e.getPath().getPathCount();
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int count = e.getClickCount();
                if (count >= 2 && UserView.count == 3 && !"本人".equals(path)) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
//							此处进入到聊天窗口，传入用户参数，和接收人参数
                            MsgView.createMsgView("QQ", "friend", path.replace("(", " ").replace(")", " ").split(" ")[1], uv.client, uv.user.getAccount());
                        }
                    }).start();
                    ;
                }
            }
        });

        jp.add(tree);
        jp.setBackground(new Color(0xffffff));
        jp.setBounds(0, 40, 400, 460);
        jp.setVisible(true);
        return jp;
    }

    /**
     * 群列表
     * @param bgcolor
     * @param uv
     * @return
     */
    @SuppressWarnings("unchecked")
    private static JPanel createGroupList(int bgcolor, final UserView uv) {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        JTree tree = null;
        DefaultMutableTreeNode group = new DefaultMutableTreeNode("群列表");
//		DefaultMutableTreeNode group1 = new DefaultMutableTreeNode("fsc"); 
//		DefaultMutableTreeNode group2 = new DefaultMutableTreeNode("zyf");
//		DefaultMutableTreeNode group3 = new DefaultMutableTreeNode("000");
//		DefaultMutableTreeNode group4 = new DefaultMutableTreeNode("111");
//		group.add(group1);
//		group.add(group2);
//		group.add(group3);
//		group.add(group4);

        int userid = uv.user.getId();
        List<Group> groups = ServiceFactory.getService("group").listPart("" + userid);
        Iterator<Group> it = groups.iterator();
        if (groups.size() > 0) {
            while (it.hasNext()) {
                Group groupitem = it.next();
                DefaultMutableTreeNode item = new DefaultMutableTreeNode(groupitem.getName() + "(" + groupitem.getAccount() + ")");
                group.add(item);
            }
        }
        tree = new JTree(group);
        tree.setBounds(20, 10, 400, 450);
        tree.setFont(new Font("宋体", Font.BOLD, 15));
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                groupPath = e.getPath().getLastPathComponent().toString();
                groupCount = e.getPath().getPathCount();
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int count = e.getClickCount();
                if (count >= 2 && groupCount == 2) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
//							此处进入到群聊界面，并传入群信息和用户信息
                            MsgView.createMsgView("QQ", "group", groupPath.replace("(", " ").replace(")", " ").split(" ")[1], uv.client, uv.user.getAccount());
                        }
                    }).start();

                }
            }
        });

        jp.add(tree);
        jp.setBackground(new Color(0xffffff));
        jp.setBounds(0, 40, 400, 460);
        jp.setVisible(false);
        return jp;
    }

    //动态
    private static JPanel createTextList(int bgcolor, UserView uv) {
        JPanel jp = new JPanel();
        jp.setLayout(null);
        jp.add(createTextItem(bgcolor, 0, uv));
        jp.add(createTextItem(bgcolor, 105, uv));
        jp.setBackground(new Color(bgcolor));
        jp.setBounds(0, 40, 400, 460);
        jp.setVisible(false);
        return jp;
    }

    //动态的面板
    private static JPanel createTextItem(int bgcolor, int top, UserView uv) {
        JPanel jpitem = new JPanel();
        JLabel jlicon = createIconLabel(10, 10, 50, 50, uv);
        JLabel jlusername = createNameLabel(70, 10, 200, "hello world ", bgcolor);
        JLabel jltime = createTimeLabel("2015-10-17");
        JLabel jlcontent = createTextContent("why are you so diao?");
        jpitem.add(jlicon);
        jpitem.add(jlusername);
        jpitem.add(jltime);
        jpitem.add(jlcontent);
        jpitem.setLayout(null);
        jpitem.setBounds(0, top, 400, 100);
        jpitem.setBackground(new Color(0xeeeeee));
        jpitem.setVisible(true);
        return jpitem;

    }

    //给按钮加事件
    private static void addClickEvent(final JButton jb, final JPanel jp, final JPanel[] jpArr, final String normalicon, final String clickicon, final JButton... arr) {
        jb.setIcon(new ImageIcon(normalicon));
        jb.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                arr[0].setIcon(new ImageIcon(friendBtnNormal));
                arr[1].setIcon(new ImageIcon(groupBtnNormal));
                arr[2].setIcon(new ImageIcon(textBtnNormal));
                jb.setIcon(new ImageIcon(clickicon));
                for (int i = 0; i < jpArr.length; i++) {
                    jpArr[i].setVisible(false);
                }
                jp.setVisible(true);
            }
        });

    }

    //用户头像
    public static JLabel createIconLabel(int left, int top, int width, int height, UserView uv) {
//		ImageIcon ii = new ImageIcon("C:\\Users\\fsc\\Desktop\\icon.png");
        ImageIcon ii = new ImageIcon(System.getProperty("user.dir") + uv.user.getIcon());
        ii.setImage(ii.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        JLabel jl = new JLabel(ii);
        jl.setBounds(left, top, width, height);
        return jl;
    }

    //用户基本信息
    private static JPanel createUserInfo(int bgcolor, final UserView uv) {
        JPanel jp = new JPanel();
        jp.add(createNameLabel(0, 0, 150, uv.user.getName() + "(" + uv.user.getAccount() + ")", bgcolor));
        jp.add(createTabStatus(bgcolor));
        jp.add(createUserLabel(uv.user.getLabel(), bgcolor));
        jp.setLayout(null);
        jp.setBackground(new Color(bgcolor));
        jp.setBounds(100, 20, 300, 78);
        jp.setVisible(true);
        return jp;
    }

    private static JLabel createTimeLabel(String time) {
        JLabel jl = new JLabel();
        jl.setVisible(true);
        jl.setText(time);
        jl.setFont(new Font("宋体", Font.BOLD, 13));
        jl.setBounds(70, 30, 80, 30);
        return jl;
    }

    private static JLabel createTextContent(String content) {
        JLabel jl = new JLabel();
        jl.setText(content);
        jl.setFont(new Font("宋体", Font.BOLD, 13));
        jl.setBounds(30, 60, 400, 30);
        return jl;
    }

    public static JLabel createNameLabel(int left, int top, int width, String name, int bgcolor) {
        JLabel jl = new JLabel();
        jl.setText(name);
        jl.setFont(new Font("宋体", Font.BOLD, 15));
        jl.setBounds(left, top, width, 30);
        return jl;
    }

    private static JComboBox<String> createTabStatus(int bgcolor) {
        JComboBox<String> jcb = new JComboBox<String>();
        String[] str = {"上线", "下线", "忙碌", "有空"};
        for (int i = 0; i < str.length; i++) {
            jcb.addItem(str[i]);
        }
        jcb.setBackground(new Color(bgcolor));
        jcb.setBounds(150, 0, 60, 30);
        return jcb;
    }

    private static JTextField createUserLabel(String label, final int bgcolor) {
        final JTextField jtf = new JTextField();
        jtf.setText(label);
        jtf.setBorder(new MyBorder(bgcolor));
        jtf.setBackground(new Color(bgcolor));
        jtf.setBounds(0, 40, 200, 30);
        jtf.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if (bgcolor == 0xffffff) {
                    jtf.setBackground(new Color(0xeeeeee));
                } else {
                    jtf.setBackground(Color.WHITE);
                }
                jtf.setSelectionStart(0);
                jtf.setSelectedTextColor(new Color(0x0B4D9B));
                jtf.setSelectionEnd(jtf.getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                jtf.setBackground(new Color(bgcolor));
            }
        });
        jtf.addMouseMotionListener(new MouseAdapter() {


            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() > 10 && e.getX() < 190 && e.getY() < 25 && e.getY() > 5) {
                    if (bgcolor == 0xffffff) {
                        jtf.setBorder(new MyBorder(0xcccccc));
                    } else {
                        jtf.setBorder(new MyBorder(0xffffff));
                    }
                } else {
                    if (bgcolor == 0xffffff) {
                        jtf.setBorder(new MyBorder(0xffffff));
                    } else {
                        jtf.setBorder(new MyBorder(0xeeeeee));
                    }
                }
            }
        });
        return jtf;
    }

    //搜索按钮
    public static JTextField createSearchInput(final int bgcolor) {
        final JTextField jtf = new JTextField();
        jtf.setText("点击搜索:联系人,群");
        jtf.setBackground(new Color(bgcolor));
        jtf.setFont(new Font("宋体", Font.ITALIC, 15));
        jtf.setForeground(new Color(0x424E5B));
        jtf.setBounds(2, 110, 400, 40);
        if (bgcolor == 0xffffff) {
            jtf.setBorder(new MyBorder(0xeeeeee));
        } else {
            jtf.setBorder(new MyBorder(0xffffff));
        }
        jtf.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if ("点击搜索:联系人,群".equals(jtf.getText())) {
                    jtf.setText("");
                }
                if (bgcolor == 0xffffff) {
                    jtf.setBackground(new Color(0xeeeeee));
                } else {
                    jtf.setBackground(Color.WHITE);
                }
                jtf.setSelectionStart(0);
                jtf.setSelectedTextColor(new Color(0x0B4D9B));
                jtf.setSelectionEnd(jtf.getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(jtf.getText())) {
                    jtf.setText("点击搜索:联系人,群");
                }
                jtf.setBackground(new Color(bgcolor));
            }
        });
        return jtf;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
