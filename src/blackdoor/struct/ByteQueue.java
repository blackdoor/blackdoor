/**
 * 
 */
package blackdoor.struct;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;

import blackdoor.util.Misc;

/**
 * @author nfischer3
 *
 */
public class ByteQueue {
	private byte[] array;
	private int start;
	private int end;
	private boolean resizable;
	
	public static void main(){
		ByteQueue q = new ByteQueue(10);
		q.setResizable(true);
		for ( int value = 0; value < 9; ++ value)
	       q.enQueue(new byte[]{(byte) value});
		System.out.println(q);
		q.deQueue(5);
		System.out.println(q.details());
		q.enQueue(new byte[]{20, 21, 22});
		System.out.println(q.details());
		q.deQueue(5);
		System.out.println(q.details());
		for ( int value = 0; value < 30; ++ value)
			q.enQueue(new byte[]{(byte) value});
		System.out.println(q.details());
		q.deQueue(15);
		System.out.println(q.details());
		q.trim();
		System.out.println(q.details());
	}
	
	public ByteQueue(){
		array = new byte[100];
		resizable = false;
		zero();
	}
	public ByteQueue(int size){
		array = new byte[size];
		resizable = false;
		zero();
	}
	
	public boolean isFull(){
		int endmod = (end + 1) % array.length;
		//System.out.println("start : " + start + " end: " + end + " endmod: " + endmod + " size: " + size());	
		return endmod == start;
	}
	
	private boolean isEmpty(){
		return end == start;
	}
	
	private void zero(){
		start = 0;
		end = 0;
	}
	
	public void resize(int newSize){
		byte[] newArray = new byte[newSize];
		int filled = filled();
		deQueue(newArray, 0, filled);
		array = newArray;
		start = 0;
		end = filled;
	}
	
	public void trim(){
		resize(filled());
	}
	
	public void enQueue(byte[] src){
		enQueue(src, 0, src.length);
	}
	
	public void enQueue(byte[] src, int offset, int length){
		if(length > capacity() - filled()){
			if(!resizable)
				throw new BufferOverflowException();
			else{
				resize(length + array.length);
			}
		}
		if(length > array.length - end){
			System.arraycopy(src, offset, array, end, array.length - end);
			//System.out.println(details());
			//System.out.println("src, " + (array.length - end) + " ," + (length - (array.length - end)));
			System.arraycopy(src, offset + array.length - end, array, 0, length - (array.length - end));
		}
		else
			System.arraycopy(src, offset, array, end, length);
		end = (end + length) % array.length;
		//System.out.println(Misc.bytesToHex(array));
	}
	
	public byte[] deQueue(int length){
		byte[] ret = new byte[length];
		deQueue(ret, 0, length);
		return ret;
	}
	
	public void deQueue(byte[] dest, int offset, int length){
		if(length > filled()){
			throw new BufferUnderflowException();
		}
		if(length > filled()){
			System.arraycopy(array, start, dest, offset, capacity() - start);
			System.arraycopy(array, 0, dest, offset + capacity() - start , length - capacity() - start);
		}
		else
			System.arraycopy(array, start, dest, offset, length);
		start = (start + length) % array.length;
	}
	
	public int filled(){
		if(start > end)
			return array.length-start+end;
		else return end - start;
	}
	
	/**
	 * 
	 * @return the number of elements that can be stored in this buffer
	 */
	public int capacity(){
		return array.length-1;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public String details(){
		return "start = " + start + " end = " + end + " array = " + Misc.bytesToHex(array) + "\n" + toString();
	}
	
	@Override
	public String toString() {
		String ret = "ByteQueue [array.length = " + array.length + " capacity = " + capacity() + " filled = " + filled() +" buffer = ";
		if(end > start)
			ret = ret + Misc.bytesToHex(Arrays.copyOfRange(array, start, end));
		else{
			ret = ret + Misc.bytesToHex(Arrays.copyOfRange(array, start, array.length));
			ret = ret + Misc.bytesToHex(Arrays.copyOfRange(array, 0, end));
		}
		return ret + "]";
	}
	
}
