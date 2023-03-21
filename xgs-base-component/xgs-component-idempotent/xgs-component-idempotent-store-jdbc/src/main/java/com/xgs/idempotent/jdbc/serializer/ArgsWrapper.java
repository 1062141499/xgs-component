package com.xgs.idempotent.jdbc.serializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xiongguoshuang
 * @date 2023-02-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArgsWrapper implements Serializable {

    Object[] args;
}
