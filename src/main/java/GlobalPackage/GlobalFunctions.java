package GlobalPackage;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static GlobalPackage.LogMethods.WriteLogError;

public class GlobalFunctions
{
    public static boolean isFileReadable(File file, int numberOfTests)
    {
        return isFileReadable(file, numberOfTests, 500);
    }

    public static boolean isFileReadable(File file, int numberOfTests, int sleepMillis)
    {
        try{
            int i = 0;
            do
            {//Placing seep here because if the file is still being written, then it will have time to end it
                if(file.exists() && file.isFile() && file.canRead())
                    return true;
                else
                {
                    TimeUnit.MILLISECONDS.sleep(sleepMillis);
                    WriteLogError("\nCould not read file at attempt: "+i+1);
                }
                i++;
            }while (i<numberOfTests);
        }catch (Exception e)
        {
            WriteLogError("GlobalFunctions::testFile->"+e);
        }
        return false;
    }
}
