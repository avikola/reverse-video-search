package reverse_video_search;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class player
{
    // Storing list options and values here for use throughout
    private static ArrayList<String> dbOrderedList;
    private static ArrayList<Double[]> dbSimScore;
    private static ArrayList<Integer> dbMotionIndex;
    private static ArrayList<Integer> dbColorIndex;

    // Input Query
    private static String img_input, audio_input;
    private static int total_frames;

    // Display components.
    private JButton op_play_btn, op_pause_btn, op_stop_btn, ip_play_btn, ip_stop_btn, ip_pause_btn;
    private JPanel JPanel1, input_side_panel, img_display_panel, db_img_panel;
    private JSlider output_slider;
    private JList<String> db_list;
    private JLabel jLabel1, db_img_lbl, qr_img_lbl, qr_currentF, db_currentF, motion_lbl, colour_lbl, sound_lbl;
    private JTextField qr_text;
    private int list_click_counter = 0;

    // Drawing.
    private int s1x, s2x, s3x, s4x;
    private static final JFrame frame = new JFrame("Player");

    // Audio components
    private AudioFormat op_format;
    private long op_audio_frames;
    private double op_audio_duration;
    private Clip output_clip;
    private AudioInputStream audioInputStream_output;
    private Clip input_clip;
    private double ip_audio_duration;
    private int ip_audio_frames;

    // Image components
    private final int img_width = 352;
    private final int img_height = 288;
    private final BufferedImage img = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage img1 = new BufferedImage(img_width, img_height, BufferedImage.TYPE_INT_RGB);

    // Basically converted the .form file to this function.
    private void display_elements()
    {
        JPanel1 = new JPanel();
        JPanel1.setLayout(new GridBagLayout());
        JPanel1.setBackground(new Color(-30700));
        JPanel1.setMinimumSize(new Dimension(-1, -1));
        JPanel1.setPreferredSize(new Dimension(-1, -1));

        db_img_panel = new JPanel();
        db_img_panel.setLayout(new GridBagLayout());
        db_img_panel.setAutoscrolls(false);
        db_img_panel.setBackground(new Color(-36557));
        db_img_panel.setMinimumSize(new Dimension(-1, -1));
        db_img_panel.setOpaque(true);
        db_img_panel.setPreferredSize(new Dimension(-1, -1));
        db_img_panel.setRequestFocusEnabled(true);

        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel1.add(db_img_panel, gbc);

        output_slider = new JSlider();
        output_slider.setBackground(new Color(-36557));
        output_slider.setEnabled(true);
        output_slider.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        output_slider.setForeground(new Color(-16777216));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        db_img_panel.add(output_slider, gbc);

        op_play_btn = new JButton();
        op_play_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        op_play_btn.setText("►");
        op_play_btn.setToolTipText("play");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        db_img_panel.add(op_play_btn, gbc);

        op_pause_btn = new JButton();
        op_pause_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        op_pause_btn.setText("❚❚");
        op_pause_btn.setToolTipText("pause");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        db_img_panel.add(op_pause_btn, gbc);

        op_stop_btn = new JButton();
        op_stop_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        op_stop_btn.setText("◼");
        op_stop_btn.setToolTipText("stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        db_img_panel.add(op_stop_btn, gbc);

        // Database current frames.
        db_currentF = new JLabel();
        db_currentF.setText("0 / 600");
        db_currentF.setFont(new Font("Sans-Serif", Font.BOLD,14));
        db_currentF.setForeground(new Color(-16777216));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        db_img_panel.add(db_currentF, gbc);


        // Colour Metric.
        colour_lbl = new JLabel();
        colour_lbl.setText("Color: ");
        colour_lbl.setFont(new Font("Sans-Serif", Font.BOLD,14));
        colour_lbl.setForeground(new Color(-16777216));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        db_img_panel.add(colour_lbl, gbc);

        // Motion Metric.
        motion_lbl = new JLabel();
        motion_lbl.setText("Motion: ");
        motion_lbl.setFont(new Font("Sans-Serif", Font.BOLD,14));
        motion_lbl.setForeground(new Color(-16777216));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        db_img_panel.add(motion_lbl, gbc);

        // Sound Metric.
        sound_lbl = new JLabel();
        sound_lbl.setText("Sound: ");
        sound_lbl.setFont(new Font("Sans-Serif", Font.BOLD,14));
        sound_lbl.setForeground(new Color(-16777216));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        db_img_panel.add(sound_lbl, gbc);

        jLabel1 = new JLabel();
        jLabel1.setBackground(new Color(-5023463));
        jLabel1.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        jLabel1.setForeground(new Color(-16777216));
        jLabel1.setText("Matched Videos:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        db_img_panel.add(jLabel1, gbc);

        // JList Elements!
        db_list = new JList<>();
        db_list.setBackground(new Color(-36557));
        db_list.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        db_list.setForeground(new Color(-16777216));
        final DefaultListModel<String> defaultListModel1 = new DefaultListModel<>();

        for (String db : dbOrderedList)
            defaultListModel1.addElement(db);

        db_list.setModel(defaultListModel1);
        db_list.setSelectionBackground(new Color(-721921));
        db_list.setSelectionForeground(new Color(-16777216));
        db_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        db_img_panel.add(db_list, gbc);

        if(db_list.getModel().getSize() == 0)
            jLabel1.setText("Query video has 0 matches.");

        img_display_panel = new JPanel();
        img_display_panel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        db_img_panel.add(img_display_panel, gbc);

        db_img_lbl = new JLabel();
        db_img_lbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        img_display_panel.add(db_img_lbl, gbc);

        input_side_panel = new JPanel();
        input_side_panel.setLayout(new GridBagLayout());
        input_side_panel.setBackground(new Color(-13220185));
        input_side_panel.setMinimumSize(new Dimension(-1, -1));
        input_side_panel.setPreferredSize(new Dimension(-1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel1.add(input_side_panel, gbc);

        ip_play_btn = new JButton();
        ip_play_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        ip_play_btn.setText("►");
        ip_play_btn.setToolTipText("play");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        input_side_panel.add(ip_play_btn, gbc);

        ip_stop_btn = new JButton();
        ip_stop_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        ip_stop_btn.setText("◼");
        ip_play_btn.setToolTipText("stop");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        input_side_panel.add(ip_stop_btn, gbc);

        ip_pause_btn = new JButton();
        ip_pause_btn.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        ip_pause_btn.setText("❚❚");
        ip_play_btn.setToolTipText("pause");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        input_side_panel.add(ip_pause_btn, gbc);

        // Query Current Frames.
        qr_currentF = new JLabel();
        qr_currentF.setText("Current Frame: ");
        qr_currentF.setFont(new Font("Sans-Serif", Font.BOLD,14));
        qr_currentF.setForeground(new Color(-721921));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        input_side_panel.add(qr_currentF, gbc);

        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        label1.setForeground(new Color(-721921));
        label1.setText("Query Video:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        input_side_panel.add(label1, gbc);

        qr_text = new JTextField();
        qr_text.setEditable(false);
        qr_text.setEnabled(true);
        qr_text.setFont(new Font("Sans-Serif", Font.BOLD, 14));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        input_side_panel.add(qr_text, gbc);

        qr_img_lbl = new JLabel();
        qr_img_lbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        input_side_panel.add(qr_img_lbl, gbc);

    }

    private void drawLines(int list_index)
    {
        int c_frame = dbColorIndex.get(list_index);
        int m_frame = dbMotionIndex.get(list_index);

        s1x = (m_frame*350)/600;
        s2x = ((m_frame+total_frames)*350)/600;
        s3x = (c_frame*350)/600;
        s4x = ((c_frame+total_frames)*350)/600;

        GridBagConstraints gbc;

        //Motion portion.
        JPanel test = new JPanel()
        {
            public Dimension getPreferredSize()
            {
                return  new Dimension(350, 35);
            }
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));

                // Motion
                g2.drawLine(s1x,0,s1x,17);
                g2.drawLine(s2x,0,s2x,17);
                g2.drawLine(s1x,15,s2x,15);

                // String portion:
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Font font = new Font("Sans-Serif",Font.BOLD,13);
                FontMetrics m1 = g2.getFontMetrics(font);
                String mot = "motion";
                g2.setFont(font);
                g2.drawString("motion",s1x+((s2x-s1x)-m1.stringWidth(mot))/2,13);
            }
        };
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.gridwidth=3;
        test.setOpaque(false);

        db_img_panel.add(test,gbc);
        test.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        // Color part.
        JPanel test1 = new JPanel()
        {
            public Dimension getPreferredSize()
            {
                return  new Dimension(350, 30);
            }
            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));

                // Colour
                g2.drawLine(s3x,13,s3x,30);
                g2.drawLine(s4x,13,s4x,30);
                g2.drawLine(s3x,15,s4x,15);

                // String portion:
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Font font = new Font("Sans-Serif",Font.BOLD,13);
                FontMetrics c1 = g2.getFontMetrics(font);
                String col = "color";
                g2.setFont(font);
                g2.drawString("color",s3x+((s4x-s3x)-c1.stringWidth(col))/2,27);
            }
        };
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.gridwidth=3;
        test1.setOpaque(false);
        db_img_panel.add(test1,gbc);
        test1.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

    }

    private player() throws UnsupportedAudioFileException
    {
        // Setting of components.
        display_elements();
        db_img_lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        output_slider.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        jLabel1.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        img_display_panel.setBackground(new Color(255, 113, 51));
        db_img_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        input_side_panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        colour_lbl.setBorder(BorderFactory.createEmptyBorder(0, 100, 5, 0));
        motion_lbl.setBorder(BorderFactory.createEmptyBorder(0, 100, 5, 0));
        sound_lbl.setBorder(BorderFactory.createEmptyBorder(0, 100, 30, 0));
        db_currentF.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // For the start: when nothing is selected.
        qr_currentF.setVisible(false);
        db_currentF.setVisible(false);
        colour_lbl.setVisible(false);
        motion_lbl.setVisible(false);
        sound_lbl.setVisible(false);

        op_play_btn.setEnabled(false);
        op_pause_btn.setEnabled(false);
        op_stop_btn.setEnabled(false);
        output_slider.setEnabled(false);

        try
        {
            input_side();
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        db_list.addListSelectionListener(e -> {
            // Enabling buttons once selected.
            op_play_btn.setEnabled(true);
            op_pause_btn.setEnabled(true);
            op_stop_btn.setEnabled(true);
            output_slider.setEnabled(true);
            db_currentF.setVisible(true);
            colour_lbl.setVisible(true);
            motion_lbl.setVisible(true);
            sound_lbl.setVisible(true);

            int c_frames_start = dbColorIndex.get(db_list.getSelectedIndex());
            int m_frames_start = dbMotionIndex.get(db_list.getSelectedIndex());
            colour_lbl.setText("Color:     "+String.format("%.2f",(((dbSimScore.get(db_list.getSelectedIndex())[0])/1)*100))+"%  ["+c_frames_start+" - "+(c_frames_start+total_frames)+"]");
            motion_lbl.setText("Motion:  "+String.format("%.2f",(((dbSimScore.get(db_list.getSelectedIndex())[1])/1)*100))+"%  ["+m_frames_start+" - "+(m_frames_start+total_frames)+"]");
            sound_lbl.setText("Sound:   "+String.format("%.2f",(((dbSimScore.get(db_list.getSelectedIndex())[2])/1)*100))+"%");

            // To stop the previous clip before going to next clip.
            list_click_counter++;
            if (list_click_counter > 1)
                output_clip.stop();

            String filePath = "src/reverse_video_search/database/" + db_list.getSelectedValue() + "/" + db_list.getSelectedValue() + ".wav";
            try
            {
                audioInputStream_output = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
                output_clip = AudioSystem.getClip();
                output_clip.open(audioInputStream_output);
            }
            catch (LineUnavailableException | IOException | UnsupportedAudioFileException e1)
            {
                e1.printStackTrace();
            }

            // Slider settings.
            op_format = audioInputStream_output.getFormat();
            op_audio_frames = audioInputStream_output.getFrameLength();
            op_audio_duration = (op_audio_frames + 0.0) / op_format.getFrameRate();
            output_slider.setMinimum(1);
            int temp_2 = (int) Math.round(op_audio_duration) * 30;
            output_slider.setMaximum(temp_2);
            output_slider.setValue(0);
            output_slider.setMajorTickSpacing((temp_2 * 30)-1);
            output_slider.setMinorTickSpacing(1);
            output_slider.setPaintLabels(true);
            output_slider.setPaintTicks(true);

            drawLines(db_list.getSelectedIndex());

        });

        output_slider.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!output_slider.isEnabled())
                    return;
                output_clip.stop();
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                if (!output_slider.isEnabled())
                    return;
                output_clip.stop();
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (!output_slider.isEnabled())
                    return;

                // Adjusting clip position to slider position.
                double x = output_slider.getValue();
                double total = (x / 30) * 1000000;
                output_clip.setMicrosecondPosition((long) total);

            }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }

        });

        op_play_btn.addActionListener(e -> db_play());

        ip_play_btn.addActionListener(e -> ip_play());

        op_pause_btn.addActionListener(e -> pause(output_clip));

        ip_pause_btn.addActionListener(e -> pause(input_clip));

        op_stop_btn.addActionListener(e -> stop(output_clip, 1));

        ip_stop_btn.addActionListener(e -> stop(input_clip, 0));
    }

    // Thread sleeper.
    private void waitInterval()
    {
        try
        {
            Thread.sleep(5);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void input_side() throws IOException, UnsupportedAudioFileException
    {
        // Setting of audio clip for input side.
        String qr_audio_filePath = "src/reverse_video_search/query/"+audio_input;
        qr_text.setText(img_input);
        String[] t2 = img_input.split("0");
        img_input = t2[0];

        try
        {
            AudioInputStream audioInputStream_input = AudioSystem.getAudioInputStream(new File(qr_audio_filePath).getAbsoluteFile());
            input_clip = AudioSystem.getClip();
            input_clip.open(audioInputStream_input);
            AudioFormat ip_format = audioInputStream_input.getFormat();
            ip_audio_frames = (int) Math.round(audioInputStream_input.getFrameLength()+0.0);
            ip_audio_duration = (int)((ip_audio_frames ) / ip_format.getFrameRate());
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
        }

    }

    private void db_play()
    {
        img_display_panel.setBackground(new Color(0, 0, 0));

        // If the clip reaches the end, it restarts.
        if (output_clip.getMicrosecondLength() == output_clip.getMicrosecondPosition())
            output_clip.setMicrosecondPosition(0);

        // Start the output_clip
        output_clip.start();

        // Thread to continuously change the slider position to clip position.
        Thread slideUpdate = new Thread(() -> {
            while (output_clip.isActive())
            {
                waitInterval();
                double op_currentTime = ((op_audio_duration * output_clip.getFramePosition()) / op_audio_frames) * 30;
                output_slider.setValue((int) Math.round(op_currentTime));
            }
        });
        slideUpdate.start();

        // Thread that displays image frames with respect to the slider position.
        Thread dbImgUpdate = new Thread(() -> {
            while (list_click_counter>=1)
            {
                waitInterval();
                String list_selection = db_list.getSelectedValue();
                int i_frame_no = output_slider.getValue();

                db_currentF.setText(i_frame_no+" / 600");

                try
                {

                    String filePath = "src/reverse_video_search/database/" + list_selection + "/" + list_selection + String.format("%03d", i_frame_no) + ".rgb";
                    File db_img_file = new File(filePath).getAbsoluteFile();
                    InputStream is = new FileInputStream(db_img_file);

                    long len = db_img_file.length();
                    byte[] bytes = new byte[(int) len];

                    int offset = 0, numRead;
                    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
                        offset += numRead;


                    int ind = 0;
                    for (int y = 0; y < img_height; ++y)
                        for (int x = 0; x < img_width; ++x)
                        {
                            byte r = bytes[ind];
                            byte g = bytes[ind + img_height * img_width];
                            byte b = bytes[ind + img_height * img_width * 2];

                            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                            img.setRGB(x, y, pix);
                            ind++;
                        }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                // Use labels to display the images
                db_img_lbl.setIcon(new ImageIcon(img));
            }
        });
        dbImgUpdate.start();
    }

    private void ip_play()
    {
        // Restarts clip if it has reached the end.
        if (input_clip.getMicrosecondLength() == input_clip.getMicrosecondPosition())
            input_clip.setMicrosecondPosition(0);

        // Starts the clip.
        input_clip.start();

        // Thread that displays images with respect to audio position.
        Thread ipImgUpdate = new Thread(() -> {
            while (true)
            {
                // Matching image number to position of clip.
                int ip_currentTime = (int) Math.round(((ip_audio_duration * input_clip.getFramePosition()) / ip_audio_frames) * 30);
                waitInterval();
                if (ip_currentTime < (ip_audio_duration * 30) - 1)
                    ip_currentTime++;

                qr_currentF.setText(ip_currentTime+" / " + Math.round(ip_audio_duration*30));
                qr_currentF.setVisible(true);

                try
                {
                    String filePath1 = "src/reverse_video_search/query/" + img_input + String.format("%03d", ip_currentTime) + ".rgb";
                    File ip_img_file = new File(filePath1).getAbsoluteFile();
                    InputStream is1 = new FileInputStream(ip_img_file);

                    long len = ip_img_file.length();
                    byte[] bytes = new byte[(int) len];

                    int offset = 0, numRead;
                    while (offset < bytes.length && (numRead = is1.read(bytes, offset, bytes.length - offset)) >= 0)
                        offset += numRead;

                    int ind = 0;
                    for (int y = 0; y < img_height; ++y)
                        for (int x = 0; x < img_width; ++x)
                        {
                            byte r = bytes[ind];
                            byte g = bytes[ind + img_height * img_width];
                            byte b = bytes[ind + img_height * img_width * 2];

                            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                            img1.setRGB(x, y, pix);
                            ind++;
                        }

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                qr_img_lbl.setIcon(new ImageIcon(img1));
            }
        });
        ipImgUpdate.start();
    }

    private void pause(Clip t1)
    {
        t1.stop();
    }

    private void stop(Clip t1, int x)
    {
        t1.setMicrosecondPosition(0);
        t1.stop();
        if (x == 1)
            output_slider.setValue(0);
    }


    static void main(String imgIn, String audioIn, ArrayList<String> dbList, ArrayList<Double[]> scores, ArrayList<Integer> motionInd, ArrayList<Integer> colorInd, int numFrames)
    {
        // Update class var
        System.out.println("Size of db list: "+dbList.size());
        dbOrderedList = dbList;
        dbSimScore = scores;
        dbMotionIndex = motionInd;
        dbColorIndex = colorInd;
        total_frames = numFrames;

        try
        {
            img_input = imgIn;
            audio_input = audioIn;
            frame.setPreferredSize(new Dimension(800, 800));
            frame.setContentPane(new player().JPanel1);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(false);
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
    }
}