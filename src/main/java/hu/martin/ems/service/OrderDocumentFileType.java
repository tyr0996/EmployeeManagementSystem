package hu.martin.ems.service;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.template.IContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public enum OrderDocumentFileType {
    PDF {
        @Override
        public byte[] generate(IXDocReport report, IContext ctx, OutputStream out) throws IOException, XDocReportException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.convert(ctx, Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM), baos);
            out.write(baos.toByteArray());
            return baos.toByteArray();
        }
    },
    ODT {
        @Override
        public byte[] generate(IXDocReport report, IContext ctx, OutputStream out) throws IOException, XDocReportException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.process(ctx, baos);
            out.write(baos.toByteArray());
            return baos.toByteArray();
        }
    };

    public abstract byte[] generate(IXDocReport report, IContext ctx, OutputStream out) throws IOException, XDocReportException;
}
