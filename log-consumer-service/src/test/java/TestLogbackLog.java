import java.io.File;
import java.io.InputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import es.gob.log.consumer.Criteria;
import es.gob.log.consumer.FragmentedFileReader;
import es.gob.log.consumer.LogFilter;
import es.gob.log.consumer.LogInfo;
import es.gob.log.consumer.LogReader;

public class TestLogbackLog {

	@Test
	public void test() throws Exception {

			LogInfo info;
			try (InputStream fis = ClassLoader.getSystemResourceAsStream("logback_api.loginfo")) {
				info = new LogInfo();
				info.load(fis);
			}


			final File logFile = new File("src/test/resources/logback_api.log");

			try (final AsynchronousFileChannel channel =
					AsynchronousFileChannel.open(
							logFile.toPath(),
							StandardOpenOption.READ);) {

				final LogReader reader = new FragmentedFileReader(channel, info.getCharset());
				final LogFilter filter = new LogFilter(info);

				filter.load(reader);

				final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				final Date endDate = formatter.parse("16:07:00");

				final Criteria criteria = new Criteria();
//				criteria.setStartDate(startDate.getTime());
				criteria.setEndDate(endDate.getTime());
				criteria.setLevel(2);

				filter.setCriteria(criteria);

				System.out.println("Listado de logs");

				System.out.println(new String(filter.filter(100)));

			}
	}
}
