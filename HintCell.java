/**
 *This class is made for guide cells
 *    Contains a number and its status
 */
public class HintCell
{
    private int number;
    public int status;
    public static final int empty = 0;
    public static final int wrong = -1;
    public static final int trueAnswer = 1;

    public HintCell(int number)
    {
        this.number = number;
        this.status = empty;
    }

    public int getNumber ()
    {
        return number;
    }

    public void setNumber (int number)
    {
        this.number = number;
    }

    public int getStatus ()
    {
        return status;
    }

    public void setStatus (int status)
    {
        this.status = status;
    }

    /**
     *This toString method returns different colors for different states
     * @return String
     */
    public String toString()
    {
        if (status == empty)
        {
            return "<font color=\"black\">" + number + "</font>";
        }
        else if (status == wrong)
        {
            return "<font color=\"red\">" + number + "</font>";
        }
        else
        {
            return "<font color=\"green\">" + number + "</font>";
        }
    }
}
