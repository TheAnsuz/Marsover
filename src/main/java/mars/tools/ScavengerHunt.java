

package mars.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import mars.Globals;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.MemoryAccessNotice;
import mars.util.Binary;

public class ScavengerHunt implements Observer, MarsTool
{
    private static final int GRAPHIC_WIDTH = 712;
    private static final int GRAPHIC_HEIGHT = 652;
    private static final int NUM_PLAYERS = 22;
    private static final int MAX_X_MOVEMENT = 2;
    private static final int MAX_Y_MOVEMENT = 2;
    private static final double MAX_MOVE_DISTANCE = 2.5;
    private static final int ENERGY_AWARD = 20;
    private static final int ENERGY_PER_MOVE = 1;
    private static final int SIZE_OF_TASK = 20;
    private static final int NUM_LOCATIONS = 7;
    private static final int START_AND_END_LOCATION = 255;
    private static final int ADMINISTRATOR_ID = 999;
    private static final int ADDR_AUTHENTICATION = -8192;
    private static final int ADDR_PLAYER_ID = -8188;
    private static final int ADDR_GAME_ON = -8184;
    private static final int ADDR_NUM_TURNS = -8180;
    private static final int ADDR_BASE = -32768;
    private static final int MEM_PER_PLAYER = 1024;
    private static final int OFFSET_WHERE_AM_I_X = 0;
    private static final int OFFSET_WHERE_AM_I_Y = 4;
    private static final int OFFSET_MOVE_TO_X = 8;
    private static final int OFFSET_MOVE_TO_Y = 12;
    private static final int OFFSET_MOVE_READY = 16;
    private static final int OFFSET_ENERGY = 20;
    private static final int OFFSET_NUMBER_LOCATIONS = 24;
    private static final int OFFSET_PLAYER_COLOR = 28;
    private static final int OFFSET_SIZE_OF_TASK = 32;
    private static final int OFFSET_LOC_ARRAY = 36;
    private static final int OFFSET_TASK_COMPLETE = 292;
    private static final int OFFSET_TASK_ARRAY = 296;
    private ScavengerHuntDisplay graphicArea;
    private int authenticationValue;
    private boolean GameOn;
    private static int SetWordCounter;
    private static int accessCounter;
    private static int playerID;
    private final boolean KENVDEBUG;
    private static PlayerData[] pd;
    private static Location[] loc;
    private Random randomStream;
    private long startTime;
    
    public ScavengerHunt() {
        this.authenticationValue = 0;
        this.GameOn = false;
        this.KENVDEBUG = false;
    }
    
    @Override
    public String getName() {
        return "ScavengerHunt";
    }
    
    @Override
    public void action() {
        final ScavengerHuntRunnable shr = new ScavengerHuntRunnable();
        final Thread t1 = new Thread(shr);
        t1.start();
        try {
            Globals.memory.addObserver(this, -32768, -16);
        }
        catch (AddressErrorException e) {
            System.out.println("\n\nScavengerHunt.action: Globals.memory.addObserver caused AddressErrorException.\n\n");
            System.exit(0);
        }
    }
    
    @Override
    public void update(final Observable o, final Object arg) {
        if (!(arg instanceof MemoryAccessNotice)) {
            return;
        }
        final MemoryAccessNotice notice = (MemoryAccessNotice)arg;
        final int address = notice.getAddress();
        final int data = notice.getValue();
        final boolean isWrite = notice.getAccessType() == 1;
        final boolean isRead = !isWrite;
        if (!isWrite) {
            return;
        }
        if (isWrite && ScavengerHunt.playerID == 999 && address == -8184) {
            this.GameOn = true;
            this.initializeScavengerData();
        }
        else if (!isWrite || address != -8192) {
            if (!isWrite || address != -8180) {
                if (isWrite && address == -8188) {
                    ++this.authenticationValue;
                    if (this.toolGetWord(-8192) == this.authenticationValue) {
                        ScavengerHunt.playerID = this.toolGetWord(-8188);
                    }
                    else {
                        System.out.println("ScavengerHunt.update(): Invalid write of player ID! \nPlayer " + ScavengerHunt.playerID + " tried to write.  Expected:   " + Binary.intToHexString(this.authenticationValue) + ", got:  " + Binary.intToHexString(this.toolGetWord(-8192)) + "\n");
                    }
                }
                else if (isWrite && address == -32768 + ScavengerHunt.playerID * 1024 + 16 && data != 0) {
                    final int energyLevel = this.toolReadPlayerData(ScavengerHunt.playerID, 20);
                    if (energyLevel <= 0) {
                        return;
                    }
                    if (this.toolReadPlayerData(ScavengerHunt.playerID, 8) < 0 || this.toolReadPlayerData(ScavengerHunt.playerID, 8) > 712 || this.toolReadPlayerData(ScavengerHunt.playerID, 12) < 0 || this.toolReadPlayerData(ScavengerHunt.playerID, 12) > 652) {
                        System.out.println("Player " + ScavengerHunt.playerID + " can't move -- out of bounds.");
                        return;
                    }
                    if (Math.sqrt(Math.pow(this.toolReadPlayerData(ScavengerHunt.playerID, 0) - this.toolReadPlayerData(ScavengerHunt.playerID, 8), 2.0) + Math.pow(this.toolReadPlayerData(ScavengerHunt.playerID, 4) - this.toolReadPlayerData(ScavengerHunt.playerID, 12), 2.0)) > 2.5) {
                        System.out.println("Player " + ScavengerHunt.playerID + " can't move -- exceeded max. movement.");
                        System.out.println("    Player is at (" + this.toolReadPlayerData(ScavengerHunt.playerID, 0) + ", " + this.toolReadPlayerData(ScavengerHunt.playerID, 4) + "), wants to go to (" + this.toolReadPlayerData(ScavengerHunt.playerID, 8) + "," + this.toolReadPlayerData(ScavengerHunt.playerID, 12) + ")");
                        return;
                    }
                    this.toolWritePlayerData(ScavengerHunt.playerID, 0, this.toolReadPlayerData(ScavengerHunt.playerID, 8));
                    this.toolWritePlayerData(ScavengerHunt.playerID, 4, this.toolReadPlayerData(ScavengerHunt.playerID, 12));
                    ScavengerHunt.pd[ScavengerHunt.playerID].setWhereAmI(this.toolReadPlayerData(ScavengerHunt.playerID, 0), this.toolReadPlayerData(ScavengerHunt.playerID, 4));
                    this.toolWritePlayerData(ScavengerHunt.playerID, 20, this.toolReadPlayerData(ScavengerHunt.playerID, 20) - 1);
                    ScavengerHunt.pd[ScavengerHunt.playerID].setEnergy(this.toolReadPlayerData(ScavengerHunt.playerID, 20));
                    for (int i = 0; i < 7; ++i) {
                        if (this.toolReadPlayerData(ScavengerHunt.playerID, 0) == ScavengerHunt.loc[i].X && this.toolReadPlayerData(ScavengerHunt.playerID, 4) == ScavengerHunt.loc[i].Y) {
                            ScavengerHunt.pd[ScavengerHunt.playerID].setVisited(i);
                        }
                    }
                    final int tempPlayerID = ScavengerHunt.playerID;
                    ScavengerHunt.playerID = 999;
                    this.toolWritePlayerData(tempPlayerID, 16, 0);
                    ScavengerHunt.playerID = tempPlayerID;
                }
                else if (isWrite && address == -32768 + ScavengerHunt.playerID * 1024 + 292 && data != 0) {
                    int prevData = this.toolReadPlayerData(ScavengerHunt.playerID, 296);
                    for (int j = 1; j < 20; ++j) {
                        final int currentData = this.toolReadPlayerData(ScavengerHunt.playerID, 296 + j * 4);
                        if (prevData > currentData) {
                            System.out.println("Whoops! Player has NOT completed task correctly");
                            return;
                        }
                        prevData = currentData;
                    }
                    this.toolWritePlayerData(ScavengerHunt.playerID, 20, 20);
                    this.toolWritePlayerData(ScavengerHunt.playerID, 292, 0);
                    for (int k = 0; k < 20; ++k) {
                        this.toolWritePlayerData(ScavengerHunt.playerID, 296 + k * 4, (int)(this.randomStream.nextDouble() * 2.147483647E9));
                    }
                    ScavengerHunt.pd[ScavengerHunt.playerID].setEnergy(20);
                }
                else if (isWrite && address == -32768 + ScavengerHunt.playerID * 1024 + 28) {
                    ScavengerHunt.pd[ScavengerHunt.playerID].setColor(this.toolReadPlayerData(ScavengerHunt.playerID, 28));
                }
                else if (!isWrite || address < -32768 + ScavengerHunt.playerID * 1024 || address >= -32768 + (ScavengerHunt.playerID + 1) * 1024) {
                    if (!isWrite || ScavengerHunt.playerID != 999) {
                        if (isWrite) {
                            JOptionPane.showMessageDialog(null, "ScavengerHunt.update(): Player " + ScavengerHunt.playerID + " writing outside assigned mem. loc. at address " + Binary.intToHexString(address) + " -- not implemented!");
                        }
                        else if (isRead) {}
                    }
                }
            }
        }
    }
    
    private void toolSetWord(final int address, final int data) {
        if (this.KENVDEBUG) {
            System.out.println("   ScavengerHunt.toolSetWord: Setting MIPS Memory[" + Binary.intToHexString(address) + "] to " + Binary.intToHexString(data) + " = " + data);
        }
        ++ScavengerHunt.SetWordCounter;
        try {
            Globals.memory.setWord(address, data);
        }
        catch (AddressErrorException e2) {
            System.out.println("ScavengerHunt.toolSetWord: deliberate exit on AEE exception.");
            System.out.println("     SetWordCounter = " + ScavengerHunt.SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.out.println("     data = " + data);
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("ScavengerHunt.toolSetWord: deliberate exit on " + e.getMessage() + " exception.");
            System.out.println("     SetWordCounter = " + ScavengerHunt.SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.out.println("     data = " + data);
            System.exit(0);
        }
        if (this.KENVDEBUG) {
            final int verifyData = this.toolGetWord(address);
            if (verifyData != data) {
                System.out.println("\n\nScavengerHunt.toolSetWord: Can't verify data! Special exit.");
                System.out.println("     address = " + Binary.intToHexString(address));
                System.out.println("     data = " + data);
                System.out.println("     verifyData = " + verifyData);
                System.exit(0);
            }
            else {
                System.out.println("  ScavengerHunt.toolSetWord: Mem[" + Binary.intToHexString(address) + " verified as " + Binary.intToHexString(data));
            }
        }
    }
    
    private int toolGetWord(final int address) {
        try {
            final int returnValue = Globals.memory.getWord(address);
            return returnValue;
        }
        catch (AddressErrorException e2) {
            System.out.println("ScavengerHunt.toolGetWord: deliberate exit on AEE exception.");
            System.out.println("     SetWordCounter = " + ScavengerHunt.SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("ScavengerHunt.toolGetWord: deliberate exit on " + e.getMessage() + " exception.");
            System.out.println("     SetWordCounter = " + ScavengerHunt.SetWordCounter);
            System.out.println("     address = " + Binary.intToHexString(address));
            System.exit(0);
        }
        return 0;
    }
    
    private int toolReadPlayerData(final int p, final int offset) {
        if (this.KENVDEBUG) {
            System.out.println("ScavengerHunt.toolReadPlayerData: called with player " + p + ", offset = " + Binary.intToHexString(offset) + " ---> address " + Binary.intToHexString(-32768 + p * 1024 + offset));
        }
        final int returnValue = this.toolGetWord(-32768 + p * 1024 + offset);
        if (this.KENVDEBUG) {
            System.out.println("ScavengerHunt.toolReadPlayerData: Mem[" + Binary.intToHexString(-32768 + p * 1024 + offset) + "] = " + Binary.intToHexString(returnValue) + " --- returning normally");
        }
        return returnValue;
    }
    
    private void toolWritePlayerData(final int p, final int offset, final int data) {
        final int address = -32768 + p * 1024 + offset;
        if (this.KENVDEBUG) {
            System.out.println("ScavengerHunt.toolWritePlayerData: called with player " + p + ", offset = " + Binary.intToHexString(offset) + ", data = " + Binary.intToHexString(data));
        }
        this.toolSetWord(address, data);
        if (this.KENVDEBUG) {
            final int verifyData = this.toolGetWord(address);
            if (data != verifyData) {
                System.out.println("\n\nScavengerHunt.toolWritePlayerData: MAYDAY data not verified !");
                System.out.println("      requested data to be written was " + Binary.intToHexString(data));
                System.out.println("      actual data at that loc is " + Binary.intToHexString(this.toolGetWord(address)));
                System.exit(0);
            }
            else {
                System.out.println("  ScavengerHunt.toolWritePlayerData: Mem[" + Binary.intToHexString(address) + " verified as " + Binary.intToHexString(data));
            }
        }
    }
    
    private void initializeScavengerData() {
        this.authenticationValue = 0;
        ScavengerHunt.playerID = 999;
        this.startTime = System.currentTimeMillis();
        this.randomStream = new Random(42L);
        for (int j = 0; j < 6; ++j) {
            ScavengerHunt.loc[j] = new Location();
            ScavengerHunt.loc[j].X = (int)(this.randomStream.nextDouble() * 712.0);
            ScavengerHunt.loc[j].Y = (int)(this.randomStream.nextDouble() * 602.0);
        }
        ScavengerHunt.loc[6] = new Location();
        ScavengerHunt.loc[6].X = 255;
        ScavengerHunt.loc[6].Y = 255;
        for (int i = 0; i < 22; ++i) {
            ScavengerHunt.pd[i] = new PlayerData();
            for (int k = 0; k < 7; ++k) {
                this.toolWritePlayerData(i, 36 + k * 8 + 0, ScavengerHunt.loc[k].X);
                this.toolWritePlayerData(i, 36 + k * 8 + 4, ScavengerHunt.loc[k].Y);
            }
            for (int k = 0; k < 20; ++k) {
                this.toolWritePlayerData(i, 296 + k * 4, (int)(this.randomStream.nextDouble() * 2.147483647E9));
            }
        }
    }
    
    static {
        ScavengerHunt.SetWordCounter = 0;
        ScavengerHunt.accessCounter = 0;
        ScavengerHunt.playerID = 999;
        ScavengerHunt.pd = new PlayerData[22];
        ScavengerHunt.loc = new Location[7];
    }
    
    private class Location
    {
        public int X;
        public int Y;
    }
    
    private class PlayerData
    {
        int whereAmIX;
        int whereAmIY;
        int energy;
        int color;
        long finishTime;
        boolean[] hasVisitedLoc;
        boolean finis;
        
        private PlayerData() {
            this.whereAmIX = 255;
            this.whereAmIY = 255;
            this.energy = 20;
            this.color = 0;
            this.hasVisitedLoc = new boolean[7];
            this.finis = false;
        }
        
        public void setWhereAmI(final int gX, final int gY) {
            this.whereAmIX = gX;
            this.whereAmIY = gY;
        }
        
        public void setEnergy(final int e) {
            this.energy = e;
        }
        
        public void setColor(final int c) {
            this.color = c;
        }
        
        public int getWhereAmIX() {
            return this.whereAmIX;
        }
        
        public int getWhereAmIY() {
            return this.whereAmIY;
        }
        
        public int getColor() {
            return this.color;
        }
        
        public boolean hasVisited(final int i) {
            return this.hasVisitedLoc[i];
        }
        
        public void setVisited(final int i) {
            this.hasVisitedLoc[i] = true;
        }
        
        public void setFinished() {
            this.finis = true;
        }
        
        public boolean isFinished() {
            return this.finis;
        }
        
        public long getFinishTime() {
            return this.finishTime;
        }
        
        public long getFinishMin() {
            return this.finishTime / 60000L;
        }
        
        public long getFinishSec() {
            return this.finishTime % 60000L / 1000L;
        }
        
        public long getFinishMillisec() {
            return this.finishTime % 1000L;
        }
        
        public void setFinishTime(final long t) {
            this.finishTime = t;
        }
        
        public int getEnergy() {
            return this.energy;
        }
    }
    
    private class ScavengerHuntRunnable implements Runnable
    {
        JPanel panel;
        
        public ScavengerHuntRunnable() {
            final JDialog frame = new JDialog(Globals.getGui(), "ScavengerHunt");
            this.panel = new JPanel(new BorderLayout());
            ScavengerHunt.this.graphicArea = new ScavengerHuntDisplay(712, 652);
            final JPanel buttonPanel = new JPanel();
            final JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    ScavengerHunt.this.graphicArea.clear();
                    ScavengerHunt.this.initializeScavengerData();
                }
            });
            buttonPanel.add(resetButton);
            this.panel.add(ScavengerHunt.this.graphicArea, "Center");
            this.panel.add(buttonPanel, "South");
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            frame.getContentPane().add(this.panel);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
            frame.setTitle(" This is the ScavengerHunt");
            frame.setDefaultCloseOperation(3);
            frame.setPreferredSize(new Dimension(712, 652));
            frame.setVisible(true);
        }
        
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ex) {}
                this.panel.repaint();
            }
        }
    }
    
    private class ScavengerHuntDisplay extends JPanel
    {
        private final int width;
        private final int height;
        private boolean clearTheDisplay;
        
        public ScavengerHuntDisplay(final int tw, final int th) {
            this.clearTheDisplay = true;
            this.width = tw;
            this.height = th;
        }
        
        public void redraw() {
            this.repaint();
        }
        
        public void clear() {
            this.clearTheDisplay = true;
            this.repaint();
        }
        
        public void paintComponent(final Graphics g) {
            final Graphics2D g2 = (Graphics2D)g;
            if (!ScavengerHunt.this.GameOn) {
                g2.setColor(Color.lightGray);
                g2.fillRect(0, 0, this.width - 1, this.height - 1);
                g2.setColor(Color.black);
                g2.drawString(" ScavengerHunt not yet initialized by MIPS administrator program.", 100, 200);
                return;
            }
            g2.setColor(Color.lightGray);
            g2.fillRect(0, 0, this.width - 1, this.height - 1);
            for (int i = 0; i < 7; ++i) {
                final int xCoord = ScavengerHunt.loc[i].X;
                final int yCoord = ScavengerHunt.loc[i].Y;
                g2.setColor(Color.blue);
                g2.fillRect(xCoord, yCoord, 20, 20);
                g2.setColor(Color.white);
                g2.drawString(" " + i, xCoord + 4, yCoord + 15);
            }
            g2.setColor(Color.black);
            g2.drawString("Player", this.width - 160, 30);
            g2.drawString("Locations", this.width - 110, 30);
            g2.drawString("Energy", this.width - 50, 30);
            g2.drawLine(this.width - 160, 35, this.width - 10, 35);
            g2.drawLine(this.width - 120, 35, this.width - 120, 365);
            g2.drawLine(this.width - 50, 35, this.width - 50, 365);
            for (int i = 0; i < 22; ++i) {
                g2.setColor(new Color(ScavengerHunt.pd[i].getColor()));
                final int xCoord = ScavengerHunt.pd[i].getWhereAmIX();
                final int yCoord = ScavengerHunt.pd[i].getWhereAmIY();
                g2.drawOval(xCoord, yCoord, 20, 20);
                g2.drawString(" " + i, xCoord + 4, yCoord + 15);
                g2.setColor(Color.black);
                g2.drawString(" " + i, this.width - 150, 50 + i * 15);
                g2.drawString(" " + ScavengerHunt.pd[i].getEnergy(), this.width - 40, 50 + i * 15);
                if (ScavengerHunt.pd[i].isFinished()) {
                    g2.drawString(ScavengerHunt.pd[i].getFinishMin() + ":" + ScavengerHunt.pd[i].getFinishSec() + ":" + ScavengerHunt.pd[i].getFinishMillisec(), this.width - 115, 50 + i * 15);
                }
                else {
                    int visCount = 0;
                    for (int j = 0; j < 7; ++j) {
                        if (ScavengerHunt.pd[i].hasVisited(j)) {
                            ++visCount;
                        }
                    }
                    if (visCount == 7) {
                        ScavengerHunt.pd[i].setFinished();
                        ScavengerHunt.pd[i].setFinishTime(System.currentTimeMillis() - ScavengerHunt.this.startTime);
                    }
                    else {
                        for (int j = 0; j < 7; ++j) {
                            if (ScavengerHunt.pd[i].hasVisited(j)) {
                                g2.fillRect(this.width - 120 + j * 10, 42 + i * 15, 10, 8);
                            }
                        }
                    }
                }
            }
        }
    }
}
