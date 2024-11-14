package cn.octopusyan.dmt.common.base;

import lombok.Setter;

/**
 * View Model
 *
 * @author octopus_yan
 */
@Setter
public abstract class BaseViewModel<VM extends BaseViewModel<VM, T>, T extends BaseController<VM>> {

    protected T controller;

}
