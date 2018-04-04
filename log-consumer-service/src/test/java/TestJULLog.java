import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.FragmentedFileReader;
import es.gob.log.consumer.LogFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

public class TestJULLog {

	@Test
	public void test() throws Exception {

			final File logFile = new File("src/test/resources/logging_api.log");

			try (final AsynchronousFileChannel channel =
					AsynchronousFileChannel.open(
							logFile.toPath(),
							StandardOpenOption.READ);) {

				final LogReader reader = new FragmentedFileReader(channel, StandardCharsets.UTF_8);
				final LogFilter filter = new LogFilter(new LogInfo());

				filter.load(reader);

				final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				final Date startDate = formatter.parse("2018-03-28 12:41:13");
				final Date endDate = formatter.parse("2018-03-28 12:41:50");

				final Criteria criteria = new Criteria();
				criteria.setStartDate(startDate.getTime());
				criteria.setEndDate(endDate.getTime());
				criteria.setLevel(5);	// WARNING y superior

				filter.setCriteria(criteria);

				System.out.println("Listado de logs");

				System.out.println(new String(filter.filter(100)));

			}
	}
}
