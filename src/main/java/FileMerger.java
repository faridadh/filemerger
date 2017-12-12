import com.google.common.collect.Maps;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;


public class FileMerger {
    private final static Logger logger = LoggerFactory.getLogger(FileMerger.class);

    @Option (name="-i",usage="Input File", required = true)
    private File recursive;

    @Option(name="-o",usage="Output File", required = true)
    private File out;

    public static void main(String[] args){
        try{
            FileMerger merger = new FileMerger();

//            merger.init(args);

            CamelContext context = new DefaultCamelContext();

            context.setTracing(true);
            context.addRoutes(new FileMergerRouteBuilder());
            context.start();
            Thread.sleep(10000);
            context.stop();
        } catch(Exception e){
            logger.error("Failed to run FileMerger", e);
            return;
        }
    }

    private void run() {

    }

    private void init(String[] args)
            throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.parseArgument(args);
    }

    private static class FileMergerRouteBuilder extends RouteBuilder {

        public void configure()
                throws Exception {
            CsvDataFormat csv = new CsvDataFormat()
                    .setHeaderDisabled(false)
                    .setSkipHeaderRecord(false)
                    .setUseMaps(true);

            from("file:data/inbox/?fileName=test.csv&noop=true")
                    .unmarshal(csv)
                    .bean(new MyCsvHandler(), "doSomething")
                    .marshal(csv)
                    .to("file:data/outbox/?fileName=result.csv");
        }
    }

    public static class MyCsvHandler {
        public void doSomething(Exchange exchange){
            List<Map<String, String>> body = (List<Map<String, String>>) exchange.getIn().getBody();
            body.get(0).put("extra", "3");
        }
    }
}
