package org.bonitasoft.extension;

import org.bonitasoft.engine.exception.BonitaException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.bonitasoft.engine.api.APIAccessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFMerger {
    private final APIAccessor apiAccessor;
    private final long processInstanceId;

    public PDFMerger(APIAccessor apiAccessor, long processInstanceId) {
        this.apiAccessor = apiAccessor;
        this.processInstanceId = processInstanceId;
    }

    public DocumentValue mergePDFs(List<Document> docs, String finalDocumentName) throws IOException {
        ByteArrayOutputStream outputStream = null;
		
		if(docs == null || docs.isEmpty()) {
            throw new IllegalArgumentException("Document List cannot be null or empty");
        }
		
		if(apiAccessor == null) {
            throw new IllegalArgumentException("apiAccessor cannot be null");
        }
		
        try {
            outputStream = new ByteArrayOutputStream();
            PDFMergerUtility PDFmerger = new PDFMergerUtility();

            for (Document doc : docs) {
				try {
					if (doc.getContentFileName().toLowerCase().endsWith(".pdf")) {   
						byte[] fileContent = apiAccessor.getProcessAPI().getDocumentContent(doc.getContentStorageId());
						PDFmerger.addSource(new ByteArrayInputStream(fileContent));
					}
				}catch(BonitaException e) {
					e.printStackTrace();
				}
            }

            PDFmerger.setDestinationStream(outputStream);
            PDFmerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            return new DocumentValue(outputStream.toByteArray(), "application/pdf", finalDocumentName);

        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
