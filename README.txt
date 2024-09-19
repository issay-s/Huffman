README.txt Write up will be an analysis of your experiments. 

What kinds of file lead to lots of compressions? 
 - files with a lot of compression would typically be large files with many repeated bytes. 
 These repeated bytes would be given very short encodings and therefore would take up
 much less space in the compressed version. This scales with the size of the file. 


What kind of files had little or no compression? 

Small files with lots of distinct bytes would have very little/negative compression. This may be due
to the size of the header (for example in SCF it is set to 32 * 256 bits which may be larger than
the original file itself), or if lots of bytes are distinct then having shorter encodings for 
different bytes would not necessarily help make the file shorter. 

What happens when you try and compress a huffman code file?

for small files (we used the eerie file), compressing SCF format .hf files may reduce size if you use STF for your 
recompression, since the header is much smaller, however an additional SCF would not help. 
If the first compression was STF, an additional
SCF run would not work, since the recompressed would be larger, and an additional STF would 
increase the file size (when we compressed eerie twice w/ STF it doubled in size). 

For larger files, such as the 2008 cia factbook, we found that in most situations, the files 
decreased in size, suggesting that an additional compression could result in more data saved. This
amount, however, (around 70k bytes) compared to the very large size of the file, is very small. 