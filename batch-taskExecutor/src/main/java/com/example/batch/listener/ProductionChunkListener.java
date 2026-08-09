package com.example.batch.listener;

import java.util.logging.Logger;

import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Component
public class ProductionChunkListener implements ChunkListener {
	private static final Logger log = Logger.getLogger(ProductionChunkListener.class.getName());

	public void beforeChunk(ChunkContext c) {
		log.info("Chunk start");
	}

	public void afterChunk(ChunkContext c) {
		 long readCount = c.getStepContext().getStepExecution().getReadCount();
		 log.info("⏹️ Chunk transaction committed! Cumulative Items Read: " + readCount);	  
		log.info("Chunk end");
	}

	public void afterChunkError(ChunkContext c) {
		log.info("Chunk error");
	}
}