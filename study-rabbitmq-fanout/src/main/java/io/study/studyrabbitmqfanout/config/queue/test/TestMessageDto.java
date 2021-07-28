package io.study.studyrabbitmqfanout.config.queue.test;

import java.io.Serializable;
import java.time.OffsetDateTime;
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
public class TestMessageDto{
	private Long memberId;
	private String message;
	// private OffsetDateTime createdDt;

	public static List<TestMessageDto> selectSampleMessage(int size){
		List<TestMessageDto> list = new ArrayList<>();

		for(int i=0; i<size; i++){
			final TestMessageDto messageDto = TestMessageDto.builder()
				.message("메시지 " + i)
				.memberId(Long.parseLong(String.valueOf(i)))
				// .createdDt(OffsetDateTime.now())
				.build();
			list.add(messageDto);
		}

		return list;
	}
}
