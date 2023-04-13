# One-based Counter for Sequence Number in VC Chunk

The VC is divided into multiple chunks and stored in an array initially. While creating a chunk we add the sequence number to it. Initially, we used the array index as the sequence number which was added in the chunks making it a 0-based counter.

Now, we are adding 1 to the array index when using it as a sequence number on the chunk making it a 1-based counter. After we read the chunk on the verifier side, we are decrementing the sequence number by 1 and then store in back in the array.
