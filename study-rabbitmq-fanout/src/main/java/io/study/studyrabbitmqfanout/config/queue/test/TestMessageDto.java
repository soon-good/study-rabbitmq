package io.study.studyrabbitmqfanout.config.queue.test;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class TestMessageDto implements Serializable{
	private Long memberId;
	private String message;

	public static List<TestMessageDto> selectSampleMessage(int size){
		List<TestMessageDto> list = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for(int i=0; i<size; i++){
			final TestMessageDto messageDto = TestMessageDto.builder()
				.message("메시지 " + OffsetDateTime.now().format(formatter))
				.build();
			list.add(messageDto);
		}

		return list;
	}
}
