# LZ77
LZ77 Compression algorithm

LZSS implementation.

The main difference between LZ77 and LZSS is that in LZ77 the dictionary reference could actually be longer 
than the string it was replacing. In LZSS, such references are omitted if the length is less than the "break even" point. 
Furthermore, LZSS uses one-bit flags to indicate whether the next chunk of data is a literal (byte) or a reference to an 
offset/length pair.(https://en.wikipedia.org/wiki/Lempel%E2%80%93Ziv%E2%80%93Storer%E2%80%93Szymanski)

Used: IntelliJ IDEA 2016.3(64) + JAVA 8 
