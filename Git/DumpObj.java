package gitlet;

import java.io.File;

/** A debugging class whose main program may be invoked as follows:
 *      
 */
public class DumpObj {

    /** Deserialize and apply dump to the contents of each of the files
     *  in FILES. */
    public static void main(String... files) {
        for (String fileName : files) {
            Dumpable obj = Utils.readObject(new File(fileName),
                                            Dumpable.class);
            obj.dump();
            System.out.println("---");
        }
    }
}

