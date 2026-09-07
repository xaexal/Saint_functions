package com.xaexal.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageResponse {
    private Integer id;
	private String filename;
    private String url;
}
