/*** This class is for guiding  a row or column of a cell
 * Contains an array of HintCells with  maximum length
 *
 *
 */

public class SomeHint
{
    private int length;
    private HintCell[] hintCells;
    private int counter = 0;

    public SomeHint(int length)
    {
        this.length = length;
        hintCells = new HintCell[length];
    }

    public int getLength ()
    {
        return length;
    }

    public void setLength (int length)
    {
        this.length = length;
    }

    public void addHintCell (HintCell hintCell)
    {
        hintCells[counter] = hintCell;
        counter++;
    }

    public HintCell[] getHintCells ()
    {
        return hintCells;
    }

    public void setHintCells (HintCell[] hintCells)
    {
        this.hintCells = hintCells;
    }

    public HintCell getHintCell (int index)
    {
        return hintCells[index];
    }

    /**
     * This method checks if the cells are correct or not
     * @param array
     */
    public void check(int[] array)
    {
        if (array.length == 0)
        {
            for (int i = 0; i < counter; i++)
            {
                hintCells[i].setStatus(HintCell.empty);
            }
        }

        int i;
        for (i = 0; i < array.length && i < counter; i++)
        {
            if (array[i] == hintCells[i].getNumber())
            {
                hintCells[i].setStatus(HintCell.trueAnswer);
            }
            else
            {
                hintCells[i].setStatus(HintCell.wrong);
            }
        }

        for (; i < counter; i++)
        {
            hintCells[i].setStatus(HintCell.empty);
        }
    }


    /**
     *toString method to print the help fields
     * @return toString of someHint
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append("<html>");
        for (int i = 0; i < counter; i++)
        {
            result.append(hintCells[i].toString());
            result.append("<br>");
        }

        result.append("<br>".repeat(Math.max(0, length - counter)));

        result.append("</html>");

        System.out.println(result.toString());

        return result.toString();
    }
}
