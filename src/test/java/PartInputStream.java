import javax.servlet.ServletInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

/**
 * Created by khk on 2016-05-25.
 */
class PartInputStream extends FilterInputStream {
	private String boundary;
	private byte[] buf = new byte[65536];
	private int count;
	private int pos;
	private boolean eof;

	PartInputStream(ServletInputStream in, String boundary) throws IOException {
		super(in);
		this.boundary = boundary;
	}

	private void fill() throws IOException {
		if(!this.eof) {
			if(this.count > 0) {
				if(this.count - this.pos != 2) {
					throw new IllegalStateException("fill() detected illegal buffer state");
				}

				System.arraycopy(this.buf, this.pos, this.buf, 0, this.count - this.pos);
				this.count -= this.pos;
				this.pos = 0;
			}

			boolean read = false;
			int boundaryLength = this.boundary.length();

			int var5;
			for(int maxRead = this.buf.length - boundaryLength - 2; this.count < maxRead; this.count += var5) {
				var5 = ((ServletInputStream)super.in).readLine(this.buf, this.count, this.buf.length - this.count);
				if(var5 == -1) {
					throw new IOException("unexpected end of part");
				}

				if(var5 >= boundaryLength) {
					this.eof = true;

					for(int i = 0; i < boundaryLength; ++i) {
						if(this.boundary.charAt(i) != this.buf[this.count + i]) {
							this.eof = false;
							break;
						}
					}

					if(this.eof) {
						break;
					}
				}
			}

		}
	}

	public int read() throws IOException {
		if(this.count - this.pos <= 2) {
			this.fill();
			if(this.count - this.pos <= 2) {
				return -1;
			}
		}

		return this.buf[this.pos++] & 255;
	}

	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		byte total = 0;
		if(len == 0) {
			return 0;
		} else {
			int avail = this.count - this.pos - 2;
			if(avail <= 0) {
				this.fill();
				avail = this.count - this.pos - 2;
				if(avail <= 0) {
					return -1;
				}
			}

			int copy = Math.min(len, avail);
			System.arraycopy(this.buf, this.pos, b, off, copy);
			this.pos += copy;

			int total1;
			for(total1 = total + copy; total1 < len; total1 += copy) {
				this.fill();
				avail = this.count - this.pos - 2;
				if(avail <= 0) {
					return total1;
				}

				copy = Math.min(len - total1, avail);
				System.arraycopy(this.buf, this.pos, b, off + total1, copy);
				this.pos += copy;
			}

			return total1;
		}
	}

	public int available() throws IOException {
		int avail = this.count - this.pos - 2 + super.in.available();
		return avail < 0?0:avail;
	}

	public void close() throws IOException {
		if(!this.eof) {
			while(this.read(this.buf, 0, this.buf.length) != -1) {
				;
			}
		}

	}
}
