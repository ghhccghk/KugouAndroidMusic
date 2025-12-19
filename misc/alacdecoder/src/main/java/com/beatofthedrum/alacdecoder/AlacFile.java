/*
** AlacFile.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package com.beatofthedrum.alacdecoder;

public class AlacFile
{

    final int buffer_size = 16384;
	public byte[] input_buffer;
	public int[] channel_map;
	public int numchannels = 0;
	public int bytespersample_output = 0;
	/* stuff from setinfo */
	public int setinfo_max_samples_per_frame = 0; // 0x1000 = 4096
	public int max_frame_bytes = 0;
	int ibIdx = 0;
	int input_buffer_bitaccumulator = 0; /* used so we can do arbitary
						bit reads */
    LeadingZeros lz = new LeadingZeros();
    /* buffers */
	int[][] outputsamples_buffer;
	int[] uncompressed_bytes_buffer_a = null;
	int[] uncompressed_bytes_buffer_b = null;
	int bitspersample_input = 0; // 0x10
	int setinfo_rice_historymult = 0; // 0x28
	int setinfo_rice_initialhistory = 0; // 0x0a
	int setinfo_rice_kmodifier = 0; // 0x0e
	/* end setinfo stuff */
    int[] predictor_coef_table_a = new int[1024];
    int[] predictor_coef_table_b = null;
}