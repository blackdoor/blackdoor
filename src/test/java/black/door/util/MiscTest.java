package black.door.util;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by nfischer on 5/23/15.
 */
public class MiscTest {

    @org.junit.Test
    public void misc() throws Exception{
        Object o = null;
        System.out.println((int)5l);
        Integer i = 5;
        System.out.println((double) i);

        //Cipher c = Cipher.getInstance("NULL");

        DBP.getChannel(DBP.DefaultChannelNames.DEBUG).setPrintStack(true);//.setPrintLine(true);
        DBP.enableChannel("Debug");
        DBP.printdebugln("multi\nline\nthing");
    }
}
