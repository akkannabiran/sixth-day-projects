package com.sixthday.navigation.batch.io;

import com.sixthday.navigation.batch.vo.SiloNavTreeProcessorResponse;

public interface SiloNavTreeWriter {

    void write(SiloNavTreeProcessorResponse siloNavTreeProcessorResponse);
}
