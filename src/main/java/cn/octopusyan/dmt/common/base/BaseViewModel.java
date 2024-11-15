package cn.octopusyan.dmt.common.base;

import lombok.Setter;

/**
 * View Model
 *
 * @author octopus_yan
 */
@Setter
public abstract class BaseViewModel<T extends BaseController> {

    protected T controller;

}
