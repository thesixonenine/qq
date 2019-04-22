package com.qq.view;

import com.qq.model.entity.User;
import com.qq.model.service.ServiceFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.net.Socket;

/**
 * @author fsc
 * clinet 是从类QQClient传送过来的参数。
 * 功能：构建登录界面，登录和注册，中间有很多的验证
 */
public class Login extends JFrame {

    private final Socket client;
    private static final long serialVersionUID = 1L;

    public Login(String name, Socket client) {
        super(name);
        this.client = client;
    }

    public static Login createLoginView(String name, Socket client) {
        Login login = new Login(name, client);

        ImageIcon background = new ImageIcon(System.getProperty("user.dir") + "\\src\\imgs\\qq.jpg");
        JLabel jl = new JLabel(background);
        jl.setBounds(0, 0, background.getIconWidth(), background.getIconHeight());
        JPanel jp = (JPanel) login.getContentPane();
        jp.setOpaque(false);
        jp.setLayout(null);
        JPanel userInfo = createJPanel(background, login);
        jp.add(userInfo);

        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setBounds(700, 250, 500, 500);
        login.getLayeredPane().setLayout(null);
        login.getLayeredPane().add(jl, new Integer(Integer.MIN_VALUE));
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        login.setSize(background.getIconWidth(), background.getIconHeight() + 220);
        login.setResizable(false);
        login.setVisible(true);
        return login;
    }

    private static JPanel createJPanel(ImageIcon background, Login jp) {
        JPanel userInfo = new JPanel();
        final JButton jb = new JButton("安全登录");
        final JButton registerbtn = new JButton("切换到注册");
        final JTextField jtfusername = new JTextField();
        final JTextField jtfuserpassword = new JTextField();
        JCheckBox jcb1 = new JCheckBox("记住密码");
        JCheckBox jcb2 = new JCheckBox("自动登录");
        JTextArea jlerror = new JTextArea();

        userInfo.setLayout(null);
        userInfo.setBounds(0, background.getIconHeight(), background.getIconWidth(), background.getIconHeight());
        userInfo.setBackground(Color.white);
        JLabel icon = new JLabel();
//		ImageIcon usericon  = new ImageIcon("C:\\Users\\fsc\\Desktop\\icon.png");
        ImageIcon usericon = new ImageIcon(System.getProperty("user.dir") + "\\src\\imgs\\icon.png");
        icon.setIcon(usericon);
        icon.setBounds(50, 10, usericon.getIconWidth(), usericon.getIconHeight());

        createTextFieldUser(usericon, registerbtn, jtfusername);
        createRegisterBtn(jb, usericon, registerbtn, jtfusername, jtfuserpassword, jlerror);
        createTextFieldPassword(usericon, registerbtn, jtfuserpassword);
        createCheckBox(usericon, jcb1, 70);
        createCheckBox(usericon, jcb2, 191);
        createLoginBtn(jb, registerbtn, usericon, jtfusername, jtfuserpassword, jlerror, jp);
        createErrorBlock(jlerror, usericon);

        userInfo.add(icon);
        userInfo.add(jtfusername);
        userInfo.add(registerbtn);
        userInfo.add(jtfuserpassword);
        userInfo.add(jcb1);
        userInfo.add(jcb2);
        userInfo.add(jb);
        userInfo.add(jlerror);
        return userInfo;
    }

    private static JTextArea createErrorBlock(JTextArea jlerror, ImageIcon icon) {
        jlerror.setVisible(false);
        jlerror.setEditable(false);
        jlerror.setText("text\nhello");
        jlerror.setFont(new Font("宋体", Font.ITALIC, 12));
        jlerror.setBackground(new Color(0xffffff));
        jlerror.setBounds(icon.getIconWidth() + 320, 110, 200, 44);
        return jlerror;
    }

    private static void createLoginBtn(final JButton jb, final JButton jb1, ImageIcon usericon, final JTextField username,
                                       final JTextField userpassword, final JTextArea jlerror, final Login jp) {
        jb.setBounds(usericon.getIconWidth() + 70, 116, 243, 38);
        jb.setBackground(new Color(0x3CC3F5));
        jb.setForeground(new Color(0xffffff));
        jb.setBorderPainted(false);
        jb.setFont(new Font("宋体", Font.PLAIN, 14));
        jb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String name = username.getText();
                String password = userpassword.getText();
                String type = null;
                if ("切换到注册".equals(jb1.getText())) {
                    type = "2";
                } else {
                    type = "1";
                }
                System.out.println(name + ":" + password);
                if (name.startsWith("请") || name.startsWith("注")) {
                    name = "";
                }
                if (password.startsWith("请") || password.startsWith("密")) {
                    password = "";
                }
                if (name.length() < 6 || password.length() < 6) {
                    jlerror.setText("QQ帐号或者密码格式\n不对，请重新输入");
                    jlerror.setForeground(Color.red);
                    jlerror.setVisible(true);
                    return;
                } else {
                    jlerror.setVisible(false);
                }
                BufferedWriter bw = null;
                BufferedReader br = null;
                try {
                    bw = new BufferedWriter(new OutputStreamWriter(jp.client.getOutputStream()));
                    bw.write(type + ":" + name + ":" + password + "\n");
                    bw.flush();
                    br = new BufferedReader(new InputStreamReader(jp.client.getInputStream()));
                    String result = br.readLine();
                    System.out.println(result);
                    if (result.startsWith("欢迎:")) {
                        jlerror.setVisible(true);
                        jlerror.setText("登入成功！\n2秒后跳转");
                        jlerror.setForeground(Color.GREEN);
                        jp.setVisible(false);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName());
                        final User user = (User) ServiceFactory.getService("user").findByCondition(result.split(":")[1]);
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                try {
//										创建用户注主界面，并且把socket继续传递
                                    UserView.createUserView("QQ", 0xffffff, jp.client, user.getAccount());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        ;
                        jp.dispose();

                    } else {
                        String[] res = result.split(",");
                        result = "";
                        System.out.println(res.length);
                        for (int i = 0; i < res.length; i++) {
                            result += res[i] + "\n";
                        }
                        jlerror.setVisible(true);
                        jlerror.setText(result);
                        jlerror.setForeground(Color.red);
                    }
                } catch (IOException e1) {
                    System.out.println("注册或登录失败！");
                }
            }
        });
    }

    private static void createCheckBox(ImageIcon usericon, JCheckBox jcb1, int left) {
        jcb1.setFont(new Font("宋体", Font.PLAIN, 13));
        jcb1.setBounds(usericon.getIconWidth() + left, 86, 121, 30);
        jcb1.setForeground(new Color(0x000000));
        jcb1.setBackground(Color.WHITE);
    }

    private static void createTextFieldPassword(ImageIcon usericon, final JButton tab,
                                                final JTextField jtfuserpassword) {
        jtfuserpassword.setBounds(usericon.getIconWidth() + 70, 46, 243, 38);
        jtfuserpassword.setText("请输入密码");
//		jtfuserpassword.setText("111111");
        jtfuserpassword.setFont(new Font("宋体", Font.ITALIC, 14));
        jtfuserpassword.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if ("请输入密码".equals(jtfuserpassword.getText()) || "密码(6位字符)".equals(jtfuserpassword.getText())) {
                    jtfuserpassword.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(jtfuserpassword.getText())) {
                    if ("切换到注册".equals(tab.getText())) {
                        jtfuserpassword.setText("请输入密码");
                    } else {
                        jtfuserpassword.setText("密码(6位字符)");
                    }

                }
            }

        });
    }

    private static void createRegisterBtn(final JButton jb, ImageIcon usericon, final JButton registerbtn,
                                          final JTextField username, final JTextField userpassword, final JTextArea jlerror) {
        registerbtn.setVisible(true);
        registerbtn.setBounds(usericon.getIconWidth() + 313, 10, 110, 30);
        registerbtn.setBorderPainted(false);
        registerbtn.setFont(new Font("宋体", Font.ITALIC, 14));
        registerbtn.setBackground(Color.white);
        registerbtn.setForeground(new Color(0x2685E3));
        registerbtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                jlerror.setVisible(false);
                if ("切换到注册".equals(registerbtn.getText())) {
                    registerbtn.setText("切换到登录");
                    jb.setText("安全注册");
                    username.setText("注册的QQ号(6位以上数字)");
                    userpassword.setText("密码(6位字符)");
                } else {
                    registerbtn.setText("切换到注册");
                    jb.setText("安全登录");
                    username.setText("请输入QQ号");
                    userpassword.setText("请输入密码");
                }

            }
        });
    }

    private static void createTextFieldUser(ImageIcon usericon, final JButton tab, final JTextField jtfusername) {
        jtfusername.setBounds(usericon.getIconWidth() + 70, 10, 243, 38);
        jtfusername.setText("请输入QQ号");
//		jtfusername.setText("111111");
        jtfusername.setFont(new Font("宋体", Font.ITALIC, 14));
        jtfusername.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if ("请输入QQ号".equals(jtfusername.getText()) || "注册的QQ号(6位以上数字)".equals(jtfusername.getText())) {
                    jtfusername.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("切换到注册".equals(tab.getText())) {
                    if ("".equals(jtfusername.getText())) {
                        jtfusername.setText("请输入QQ号");
                    }
                } else {
                    if ("".equals(jtfusername.getText())) {
                        jtfusername.setText("注册的QQ号(6位以上数字)");
                    }
                }

            }

        });
    }
}

class MyBorder implements Border {
    private int color;

    public MyBorder(int color) {
        this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
                            int height) {
        g.setColor(new Color(color));
        g.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 0, 0);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

}
