package speaktome.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Security {

    /*
        Function encrypts a byte array
        Input: byte array to encrypt
        Output encrypted byte array
     */
    public static byte[] encrypt(byte[] msg) throws UnsupportedEncodingException {
        byte[] encryptedByteArray = new byte[msg.length * 4 + 1]; //encrypted byte array size = 4n+1 where n is the length of the input byte array

        //first random byte and the positions for the next byte
        encryptedByteArray[0] = getCorrectByte(6, 7);
        int pos1 = getNum(encryptedByteArray[0], 0, 1, 2);
        int pos2 = getNum(encryptedByteArray[0], 3, 4, 5);

        //encrypt the byte array
        for(int i = 1; i < encryptedByteArray.length; i++) {
            //current byte
            byte temp = getCorrectByte(pos1, pos2);

            //inject bits from the msg byte array into the encrypted one
            try {
                //(i-1)/4 - the byte to take from
                //((i - 1) % 4) * 2 - the index of the bit that should be taken first from the byte (+1 for the second bit)
                temp = setBit(temp, pos1, getBit(msg[(i - 1)/4], ((i - 1) % 4) * 2));
                temp = setBit(temp, pos2, getBit(msg[(i - 1)/4], ((i - 1) % 4) * 2 + 1));
            }
            catch(Exception e) {
                System.out.println(e);
            }

            //get the positions for the next byte
            ArrayList<Integer> positions = getPositionsArray(pos1, pos2);
            pos1 = getNum(temp, positions.get(0), positions.get(1), positions.get(2));
            pos2 = getNum(temp, positions.get(3), positions.get(4), positions.get(5));

            //set the current into the encrypted byte array
            encryptedByteArray[i] = temp;
        }
        return encryptedByteArray;
    }

    /*
        Function decrypts a byte array
        Input: byte array to decrypt
        Output decrypted byte array
     */
    public static byte[] decrypt(byte[] encryptedMsg) {
        byte[] decryptedByteArray = new byte[(encryptedMsg.length - 1) / 4];  //decrypted byte array size = (n-1)/4 where n is the length of the input byte array

        //positions for the next byte
        int pos1 = getNum(encryptedMsg[0], 0, 1, 2);
        int pos2 = getNum(encryptedMsg[0], 3, 4, 5);

        for (int i = 1; i < encryptedMsg.length; i++) {
            //get the 2 bits hidden in the byte
            try {
                decryptedByteArray[(i - 1) / 4] = setBit(decryptedByteArray[(i - 1) / 4], ((i - 1) % 4) * 2, getBit(encryptedMsg[i], pos1));
                decryptedByteArray[(i - 1) / 4] = setBit(decryptedByteArray[(i - 1) / 4], ((i - 1) % 4) * 2 + 1, getBit(encryptedMsg[i], pos2));
            }
            catch(Exception e) {
                System.out.println(e);
            }

            //get positions for the next byte
            ArrayList<Integer> positions = getPositionsArray(pos1, pos2);
            pos1 = getNum(encryptedMsg[i], positions.get(0), positions.get(1), positions.get(2));
            pos2 = getNum(encryptedMsg[i], positions.get(3), positions.get(4), positions.get(5));
        }
        return decryptedByteArray;
    }

    /*
        Gets a random byte where the next positions are not equal
        Input: previous positions
        Output: the byte
     */
    private static byte getCorrectByte(int pos1, int pos2) {

        ArrayList<Integer> positions = getPositionsArray(pos1, pos2);

        byte b;
        do {
            b = getRandomByte();
            pos1 = getNum(b, positions.get(0), positions.get(1), positions.get(2));
            pos2 = getNum(b, positions.get(3), positions.get(4), positions.get(5));
        }
        while(pos1 == pos2);
        return b;
    }

    /*
        Returns an array list of positions from 0 to 7 without positions pos1 and pos2
        Input: positions to not put in the array
        Output: the array list
     */
    private static ArrayList<Integer> getPositionsArray(int pos1, int pos2) {
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < 8; i ++) {
            if(i != pos1 && i != pos2) {
                positions.add(i);
            }
        }
        return positions;
    }

    /*
        Returns a random byte
        Input: none
        Output: the random byte
     */
    private static byte getRandomByte() {
        return (byte)(Math.random() * 256);
    }

    /*
        Returns the number that the indexes pos1, pos2 and pos3 make in byte b
        Input: byte, 3 indexes
        Output: the number
     */
    private static int getNum(byte b, int pos1, int pos2, int pos3) {
        int bit1 = getBit(b, pos1);
        int bit2 = getBit(b, pos2);
        int bit3 = getBit(b, pos3);
        bit1 = bit1 << 2;
        bit2 = bit2 << 1;
        return bit1 + bit2 + bit3;
    }

    /*
        Returns a bit from a byte in index pos
        Input: byte b and the index
        Output: the bit - 0 or 1
     */
    private static int getBit(byte b, int pos) {
        pos = 7 - pos;
        return (b >> pos) & 1;
    }

    /*
        Sets a bit in a certain index
        Input: byte n, index, and value - 0 or 1
        Output: the byte with the set position
     */
    private static byte setBit(byte b, int pos, int val) throws Exception {
        pos = 7 - pos;
        if(val == 0) {
            return (byte) (b & ~(1 << pos));
        }
        else if (val == 1) {
            return (byte) (b | (1 << pos));
        }
        else {
            throw new Exception("Val can be only 1 or 0");
        }
    }
}

