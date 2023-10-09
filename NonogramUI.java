import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

public class NonogramUI extends JFrame implements ActionListener
{
    private Nonogram puzzle;
    private final Stack<Assign> stack;
    private final String address = "nons/tiny.non";
    private static final String NGFILE = "nons/tiny.non";
    private int counter = 1;
    private final int height;
    private final int width;
    private JButton[][] buttons;
    private JPanel centerSide;
    private JPanel leftSide;
    private JPanel topSide;
    private JPanel rightSide;
    private JLabel[] leftSideLabel;
    private JLabel[] topSideLabel;
    private SomeHint[] topSideHint;
    private SomeHint[] leftSideHint;



    public NonogramUI () throws InterruptedException
    {
        stack = new Stack<>();
        Scanner fs = null;
        try
        {
            fs = new Scanner(new File(NGFILE));
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(this, NGFILE + "not found");
            Thread.sleep(1000);
            System.exit(0);
        }
        puzzle = new Nonogram(fs);

        this.height = puzzle.getNumRows();
        this.width = puzzle.getNumCols();
    }


    /**
     This function performs initial settings.
     */
    public void init ()
    {
        leftSideInit();
        topSideInit();
        rightSideInit();
        centerSideInit();
        initTopLeft();
        checkHint();
        setHintText();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(50, 50));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));


        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.add(mainPanel);
        this.setSize(900, 700);


        mainPanel.add(centerSide, BorderLayout.CENTER);
        mainPanel.add(leftSide, BorderLayout.WEST);
        mainPanel.add(rightSide, BorderLayout.EAST);
        mainPanel.add(topSide, BorderLayout.NORTH);

    }

    /**
    This function adjusts the left section game area and adds labels.
     */
    public void leftSideInit ()
    {
        leftSide = new JPanel();

        GridLayout gridLayout = new GridLayout(height, 1);
        gridLayout.setVgap(5);

        leftSide.setLayout(gridLayout);

        leftSideLabel = new JLabel[height];
        for (int i = 0; i < height; i++)
        {
            leftSideLabel[i] = getJLabel();
            leftSide.add(leftSideLabel[i]);
        }
    }

    /**
    This function performs the settings of the top section game area and adds the labels
     */
    public void topSideInit ()
    {
        topSide = new JPanel();

        GridLayout gridLayout = new GridLayout(1, width + 2);

        topSide.setLayout(gridLayout);

        topSideLabel = new JLabel[width + 2];
        for (int i = 0; i < width + 2; i++)
        {
            topSideLabel[i] = getJLabel();
            topSideLabel[i].setMinimumSize(new Dimension(50, 100));
            topSide.add(topSideLabel[i]);
        }
    }

    /**
    This part makes settings related to hints
     */
    public void initTopLeft ()
    {
        // collect the nums for the rows and columns
        int numRows = puzzle.getNumRows();
        int numCols = puzzle.getNumCols();
        int[][] rowNums = new int[numRows][];
        int[][] colNums = new int[numCols][];
        int maxRowNumsLen = 0;
        int maxColNumsLen = 0;
        for (int row = 0; row < numRows; row++)
        {
            rowNums[row] = puzzle.getRowNums(row);
            if (rowNums[row].length > maxRowNumsLen)
            {
                maxRowNumsLen = rowNums[row].length;
            }
        }
        for (int col = 0; col < numRows; col++)
        {
            colNums[col] = puzzle.getColNums(col);
            if (colNums[col].length > maxColNumsLen)
            {
                maxColNumsLen = colNums[col].length;
            }
        }


        topSideHint = new SomeHint[numRows]; // hint for each row
        leftSideHint = new SomeHint[numCols]; // hint for each column
        for (int row = 0; row < numRows; row++)
        {
            topSideHint[row] = new SomeHint(maxRowNumsLen);
            for (int i = 0; i < rowNums[row].length; i++)
            {
                topSideHint[row].addHintCell(new HintCell(rowNums[row][i]));
            }
        }

        for (int col = 0; col < numCols; col++)
        {
            leftSideHint[col] = new SomeHint(maxColNumsLen);
            for (int i = 0; i < colNums[col].length; i++)
            {
                leftSideHint[col].addHintCell(new HintCell(colNums[col][i]));
            }
        }
    }

    /**
    This method checks if the cells match the hints or no
     */
    public void checkHint()
    {
        for (int row = 0; row < height; row++)
        {
            leftSideHint[row].check(puzzle.getRowNum(row));
        }

        for (int col = 0; col < width; col++)
        {
            topSideHint[col].check(puzzle.getColNum(col));
        }
    }

    /**
    This method displays hints in labels.
     */
    public void setHintText()
    {
        for (int row = 0; row < height; row++)
        {
            leftSideLabel[row].setText(leftSideHint[row].toString());
        }

        for (int col = 0; col < width; col++)
        {
            topSideLabel[col + 1].setText(topSideHint[col].toString());
        }
    }

    /**
    This function adjusts the right section game area and adds labels.
     It includes adding load, undo, clear, save buttons
     */
    public void rightSideInit ()
    {
        rightSide = new JPanel();
        GridLayout gridLayout = new GridLayout(9, 1);
        gridLayout.setVgap(10);

        // Set minimum size of the right side panel
        Dimension dimension = new Dimension(200, 0);
        rightSide.setMinimumSize(dimension);

        rightSide.setLayout(gridLayout);

        rightSide.add(createVerticalStrut(100));

        JButton clear = getButton();
        clear.setText("Clear");
        clear.addActionListener(this);
        clear.setActionCommand("Clear");
        rightSide.add(clear);

        rightSide.add(createVerticalStrut(100));

        JButton undo = getButton();
        undo.setText("Undo");
        undo.addActionListener(this);
        undo.setActionCommand("Undo");
        rightSide.add(undo);

        rightSide.add(createVerticalStrut(100));

        JButton save = getButton();
        save.setText("Save");
        save.addActionListener(this);
        save.setActionCommand("Save");
        rightSide.add(save);

        rightSide.add(createVerticalStrut(100));

        JButton load = getButton();
        load.setText("Load");
        load.addActionListener(this);
        load.setActionCommand("Load");
        rightSide.add(load);

        rightSide.add(createVerticalStrut(100));
    }

    /**
    This method performs the initial settings of the middle game section.
     */
    public void centerSideInit ()
    {
        centerSide = new JPanel();
        centerSide.setLayout(new GridLayout(height, width));
        buttons = new JButton[height][width];

        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                buttons[i][j] = getButton();
                buttons[i][j].addActionListener(this);
                buttons[i][j].setActionCommand("buttons" + i + "," + j);
                centerSide.add(buttons[i][j]);
            }
        }
    }


    /**
     main function
     */
    public static void main (String[] args)
    {
        NonogramUI nonogramUI = null;
        try
        {
            nonogramUI = new NonogramUI();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        nonogramUI.init();

        nonogramUI.setVisible(true);
    }

    /**
    This function creates a button with the specified settings.
     */
    public JButton getButton ()
    {
        JButton result = new JButton();
        result.setSize(100, 100);
        result.setMinimumSize(new Dimension(100, 100));
        result.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));

        result.setBackground(Color.white);

        return result;
    }

    /**
    This function creates a label with the specified settings bellow
     */
    public JLabel getJLabel ()
    {
        JLabel result = new JLabel("", SwingConstants.CENTER);
        result.setSize(100, 100);
        result.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        result.setBackground(Color.white);

        return result;
    }


    /**
     create an empty component with the same size as the VSpace
     */

    public Component createVerticalStrut (int height)
    {
        return Box.createRigidArea(new Dimension(0, height));
    }

    /**
    This function checks the button press by the user
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {

        if (puzzle.isSolved())
        {

            JOptionPane.showMessageDialog(this, "Puzzle is solved!");
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
            System.exit(0);
        }

        if (e.getActionCommand().equals("Clear"))
        {
            clear();
        }
        else if (e.getActionCommand().equals("Undo"))
        {
            undo();
        }
        else if (e.getActionCommand().equals("Save"))
        {
            save();
        }
        else if (e.getActionCommand().equals("Load"))
        {
            load();
        }
        else
        {
            int index = e.getActionCommand().indexOf(",");
            int i = Integer.parseInt(e.getActionCommand().substring(7, index));
            int j = Integer.parseInt(e.getActionCommand().substring(index + 1));
            Assign move;

            if (buttons[i][j].getBackground() == Color.white)
            {
                buttons[i][j].setBackground(Color.black);
                move = new Assign(i, j, Nonogram.FULL);
            }
            else
            {
                buttons[i][j].setBackground(Color.white);
                move = new Assign(i, j, Nonogram.EMPTY);
            }

            stack.push(move);
            puzzle.setState(move);
        }

        checkHint();
        setHintText();
    }

    /**
   the game is loaded by using this function
     */
    public void load ()
    {
        String address;

        address = JOptionPane.showInputDialog(this, "Enter the path:");

        File temp = new File(address);

        if (!temp.exists())
        {
            JOptionPane.showMessageDialog(this, "File does not exist!");
        }
        else
        {
            try
            {
                puzzle = new Nonogram(new Scanner(new File(address)));
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            check();
        }
    }

    /**
     * this method is created a file and write into puzzle to string as non file using toStringAsNonFile method
     */
    public void save ()
    {
        File file = new File(address + counter++ + ".non");

        try
        {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(puzzle.toStringAsNonFile());
            fileWriter.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        JOptionPane.showMessageDialog(this, "Game was saved");
    }


    /**
     * The method is used for undo function
     */
    public void undo ()
    {
        if (stack.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "There is no previous movement");
            return;
        }

        puzzle.backState(stack.pop());

        check();
    }

    /**
     * this method is clear the puzzle
     */
    public void clear ()
    {
        puzzle.clear();

        check();
    }

    /**
    This function is used to check the colors of the buttons.
     */
    public void check ()
    {
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
                if (puzzle.getState(i, j) == Nonogram.FULL)
                {
                    buttons[i][j].setBackground(Color.BLACK);
                }
                else
                {
                    buttons[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    public static char numAsChar (int i)
    {
        if (i < 0)
        {
            throw new IllegalArgumentException("i must be >= 0 (" + i + ")");
        }
        if (i < 10)
        {
            return (char) ('0' + i);
        }
        else if (i < 36)
        {
            return (char) ('A' + i - 10);
        }
        else if (i < 62)
        {
            return (char) ('a' + i - 36);
        }
        else
        {
            return '?';
        }
    }
}