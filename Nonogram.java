import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;

/**
 * A Nonogram puzzle.
 *
 * @author Dr Mark C. Sinclair
 * @version September 2022
 */
@SuppressWarnings ("deprecation")
public class Nonogram extends Observable
{
    public static final int MIN_SIZE = 5;
    public static final int EMPTY = 0;
    public static final int FULL = 1;
    public static final int UNKNOWN = 2;
    private static final boolean traceOn = false; // for debugging
    private Cell[][] cells = null;
    private Constraint[] rows = null;
    private Constraint[] cols = null;
    private int numRows = -1;
    private int numCols = -1;

    /**
     * Constructor from a scanner (.non file format)
     * see https://github.com/mikix/nonogram-db/blob/master/FORMAT.md
     *
     * @param scanner the scanner
     */
    public Nonogram (Scanner scanner)
    {
        ArrayList<NGPattern> rowNGPatterns = new ArrayList<>();
        ArrayList<NGPattern> colNGPatterns = new ArrayList<>();
        ArrayList<Assign> assigns = new ArrayList<>();
        boolean onRows = false;
        boolean onCols = false;
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("width"))
            {
                String[] fields = line.split("\\W");
                try
                {
                    numCols = Integer.parseInt(fields[1]);
                }
                catch (NumberFormatException e)
                {
                    throw new NonogramException("non-integer width (" + fields[1] + ")");
                }
                if (numCols < MIN_SIZE)
                {
                    throw new NonogramException("width cannot be shorter than " + MIN_SIZE);
                }
            }
            else if (line.startsWith("height"))
            {
                String[] fields = line.split("\\W");
                try
                {
                    numRows = Integer.parseInt(fields[1]);
                }
                catch (NumberFormatException e)
                {
                    throw new NonogramException("non-integer height (" + fields[1] + ")");
                }
                if (numRows < MIN_SIZE)
                {
                    throw new NonogramException("height cannot be shorter than " + MIN_SIZE);
                }
            }
            else if (line.startsWith("rows"))
            {
                onRows = true;
                onCols = false;
            }
            else if (line.startsWith("columns"))
            {
                onCols = true;
                onRows = false;
            }
            else if (onRows && (rowNGPatterns.size() < numRows))
            {
                String[] fields = line.split(",");
                int[] nums = new int[fields.length];
                int i = 0;
                try
                {
                    for (i = 0; i < fields.length; i++)
                    {
                        nums[i] = Integer.parseInt(fields[i].trim());
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new NonogramException("non-integer num (" + fields[i] + ")");
                }
                if (!NGPattern.checkNums(nums))
                {
                    throw new NonogramException("nums invalid");
                }
                NGPattern pat = new NGPattern(nums, numCols);
                rowNGPatterns.add(pat);
            }
            else if (onCols && (colNGPatterns.size() < numCols))
            {
                String[] fields = line.split(",");
                int[] nums = new int[fields.length];
                int i = 0;
                try
                {
                    for (i = 0; i < fields.length; i++)
                    {
                        nums[i] = Integer.parseInt(fields[i].trim());
                    }
                }
                catch (NumberFormatException e)
                {
                    throw new NonogramException("non-integer num (" + fields[i] + ")");
                }
                if (!NGPattern.checkNums(nums))
                {
                    throw new NonogramException("nums invalid");
                }
                NGPattern pat = new NGPattern(nums, numRows);
                colNGPatterns.add(pat);
            }
            else if (line.startsWith("choice"))
            {
                int size = Integer.parseInt(line.split(" ")[1]);

                for (int i = 0; i < size; i++)
                {
                    line = scanner.nextLine();

                    String[] split = line.split(" ");

                    int row = Integer.parseInt(split[0]);
                    int col = Integer.parseInt(split[1]);
                    int state = Integer.parseInt(split[2]);

                    assigns.add(new Assign(row, col, state));
                }
            }
        }

        if (rowNGPatterns.size() != numRows)
        {
            throw new NonogramException("incorrect number of rows (" + rowNGPatterns.size() + ")");
        }
        if (colNGPatterns.size() != numCols)
        {
            throw new NonogramException("incorrect number of cols (" + colNGPatterns.size() + ")");
        }

        // create grid of cells
        cells = new Cell[numRows][numCols];
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numCols; col++)
            {
                cells[row][col] = new Cell(this, row, col);
            }
        }

        // create row constraints
        rows = new Constraint[numRows];
        Cell[] rowCells = new Cell[numCols];
        for (int row = 0; row < numRows; row++)
        {
            if (numCols >= 0)
            {
                System.arraycopy(cells[row], 0, rowCells, 0, numCols);
            }
            rows[row] = new Constraint(rowNGPatterns.get(row), rowCells);
        }

        // create column constraints
        cols = new Constraint[numCols];
        Cell[] colCells = new Cell[numRows];
        for (int col = 0; col < numCols; col++)
        {
            for (int row = 0; row < numRows; row++)
            {
                colCells[row] = cells[row][col];
            }
            cols[col] = new Constraint(colNGPatterns.get(col), colCells);
        }

        for (Assign assign : assigns)
        {
            setState(assign);
        }
    }

    /**
     * A trace method for debugging (active when traceOn is true)
     *
     * @param s the string to output
     */
    public static void trace (String s)
    {
        if (traceOn)
        {
            System.out.println("trace: " + s);
        }
    }

    /**
     * Retrieve the number of rows
     *
     * @return the number of rows
     */
    public int getNumRows ()
    {
        return numRows;
    }

    /**
     * Retrieve the number of columns
     *
     * @return the number of columns
     */
    public int getNumCols ()
    {
        return numCols;
    }

    /**
     * Retrieve the state of an individual cell
     *
     * @param row the cell row
     * @param col the cell column
     * @return the cell state
     */
    public int getState (int row, int col)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        return cells[row][col].getState();
    }

    /**
     * Set the state of an individual cell, notifying observers
     *
     * @param row   the cell row
     * @param col   the cell column
     * @param state the new state
     */
    public void setState (int row, int col, int state)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        if (!Cell.isValidState(state))
        {
            throw new IllegalArgumentException("invalid state (" + state + ")");
        }
        cells[row][col].setState(state);
        trace("notifyObservers: row: " + row + "; col : " + col + "; state: " + state);
        setChanged();
        notifyObservers(cells[row][col]);
    }

    /**
     * this method is using for undo an assign
     * @param undo
     */
    public void backState(Assign undo)
    {
        int row = undo.getRow();
        int col = undo.getCol();
        int state = UNKNOWN;
        cells[row][col].setState(state);
        trace("notifyObservers: row: " + row + "; col : " + col + "; state: " + state);
        setChanged();
        notifyObservers(cells[row][col]);
    }

    /**
     * Set the state of an individual cell using the data in an Assign object
     *
     * @param move the Assign
     */
    public void setState (Assign move)
    {
        if (move == null)
        {
            throw new IllegalArgumentException("cannot have null move");
        }
        setState(move.getRow(), move.getCol(), move.getState());
    }

    /**
     * Clear all the cells in the puzzle (set to UNKNOWN)
     */
    public void clear ()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numCols; col++)
            {
                setState(row, col, UNKNOWN);
            }
        }
    }


    public int getFullCells ()
    {
        int result = 0;

        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < numCols; j++)
            {
                if (cells[i][j].isFull())
                {
                    result++;
                }
            }
        }

        return result;
    }

    public Cell[] fullCells()
    {
        Cell[] result = new Cell[getFullCells()];
        int counter = 0;

        for (int i = 0; i < numRows; i++)
        {
            for (int j = 0; j < numCols; j++)
            {
                if (cells[i][j].isFull())
                {
                    result[counter++] = cells[i][j];
                }
            }
        }

        return result;
    }

    /**
     * Retrieve the pattern of contiguous full cells for a given row as an integer array
     *
     * @param row the desired row
     * @return the pattern of contiguous full cells in the row constraint
     */
    public int[] getRowNums (int row)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        return rows[row].getNums();
    }

    /**
     * Retrieve the pattern of contiguous full cells for a given column as an integer array
     *
     * @param col the desired column
     * @return the pattern of contiguous full cells in the column constraint
     */
    public int[] getColNums (int col)
    {
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        return cols[col].getNums();
    }


    /**
    اThis method returns the number of places that are filled in a row
     For example, if the status of a line is as follows:
     1 0 0 1 1 1 0 0 1
     This method returns the following array
     1 3 1
     */
    public int[] getColNum (int col)
    {
        ArrayList<Integer> result = new ArrayList<>();
        int counter;

        for (int i = 0; i < numCols; i++)
        {
            counter = 0;
            if (cells[i][col].isFull())
            {
                for (int j = i; j < numCols; j++)
                {
                    if (cells[j][col].isFull())
                    {
                        counter++;
                    }
                    else
                    {
                        break;
                    }
                }

                result.add(counter);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }


    /**
    This method returns the number of houses that are filled in a column
     */
    public int[] getRowNum(int row)
    {
        ArrayList<Integer> result = new ArrayList<>();
        int counter;

        for (int i = 0; i < numRows; i++)
        {
            counter = 0;
            if (cells[row][i].isFull())
            {
                for (int j = i; j < numRows; j++)
                {
                    if (cells[row][j].isFull())
                    {
                        counter++;
                    }
                    else
                    {
                        break;
                    }
                }

                result.add(counter);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Retrieve the cell states for a given row as a sequence string
     *
     * @param row the desired row
     * @return the row cell states
     */
    public String getRowSequence (int row)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        return rows[row].getSequence();
    }

    /**
     * Retrieve the cell states for a given column as a sequence string
     *
     * @param col the desired column
     * @return the column cell states
     */
    public String getColSequence (int col)
    {
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        return cols[col].getSequence();
    }

    /**
     * Set the cell states of an entire nonogram from a single cell state string (e.g. the goal in a .non file)
     *
     * @param s the goal string
     */
    public void setStatesByString (String s)
    {
        if (s == null)
        {
            throw new IllegalArgumentException("s cannot be null");
        }
        if (s.isEmpty())
        {
            throw new IllegalArgumentException("s cannot be empty");
        }
        if (s.length() != numRows * numCols)
        {
            throw new IllegalArgumentException("s must be " + numRows * numCols + " chars long (" + s.length() + ")");
        }
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numCols; col++)
            {
                int idx = row * numCols + col;
                int state = Nonogram.UNKNOWN;
                try
                {
                    state = Integer.parseInt(s.substring(idx, idx + 1));
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("s contains non number (" + s.charAt(idx) + ") in s[" + idx + "]");
                }
                if (!Cell.isValidState(state))
                {
                    throw new IllegalArgumentException("invalid state (" + state + ") in s[" + idx + "]");
                }
                cells[row][col].setState(state);
            }
        }
    }

    /**
     * Is a given row of cells valid against its constraint?
     *
     * @param row the desired row
     * @return true if the row is valid, otherwise false
     */
    public boolean isRowValid (int row)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        return rows[row].isValid();
    }

    /**
     * Is a given column of cells valid against its constraint?
     *
     * @param col the desired column
     * @return true if the column is valid, otherwise false
     */
    public boolean isColValid (int col)
    {
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        return cols[col].isValid();
    }

    /**
     * Is a given row of cells solved? (Note that a row may be solved, but still incorrect depending on other columns.)
     *
     * @param row the desired row
     * @return true if the row is solved, otherwise false
     */
    public boolean isRowSolved (int row)
    {
        if ((row < 0) || (row >= numRows))
        {
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        }
        return rows[row].isSolved();
    }

    /**
     * Is a given column of cells solved? (Note that a column may be solved, but still incorrect depending on other rows.)
     *
     * @param col the desired column
     * @return true if the column is solved, otherwise false
     */
    public boolean isColSolved (int col)
    {
        if ((col < 0) || (col >= numCols))
        {
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        }
        return cols[col].isSolved();
    }

    /**
     * Are all rows and columns, and therefore the whole puzzle, solved?
     *
     * @return true if all rows and coplumns are solved, otherwise false
     */
    public boolean isSolved ()
    {
        for (int row = 0; row < numRows; row++)
        {
            if (!rows[row].isSolved())
            {
                return false;
            }
        }
        for (int col = 0; col < numCols; col++)
        {
            if (!cols[col].isSolved())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * String representation of the puzzle in .non file form
     *
     * @return the string representation
     */
    public String toStringAsNonFile ()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("width ").append(numCols).append("\n");
        sb.append("height ").append(numRows).append("\n");
        sb.append("\n");
        sb.append("rows\n");
        for (int row = 0; row < numRows; row++)
        {
            sb.append(rows[row].getNumsForNon()).append("\n");
        }
        sb.append("\n");
        sb.append("columns\n");
        for (int col = 0; col < numRows; col++)
        {
            sb.append(cols[col].getNumsForNon()).append("\n");
        }
        sb.append("\n");

        sb.append("choice ");

        int size = getFullCells();
        sb.append(size).append("\n");

        Cell[] cells = fullCells();

        for (Cell cell : cells)
        {
            sb.append(cell.getRow()).append(" ");
            sb.append(cell.getCol()).append(" ");
            sb.append(cell.getState()).append("\n");
        }

        sb.append("\n").append(goalString());

        return sb.toString();
    }

    public String goalString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("goal \"");
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numCols; col++)
            {
                sb.append(cells[row][col].getState());
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}