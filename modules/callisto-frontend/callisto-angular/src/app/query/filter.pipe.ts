import { Pipe, PipeTransform } from '@angular/core';
import { Filter } from '../model/filter'

@Pipe({
    name: 'domainFiltersFilter',
    pure: false
})

export class DomainFilterFilterPipe implements PipeTransform {
    transform(filters: any[]):any {
        if(!filters) {
            return filters;
        }
        return filters.filter(item => {
            const filter: Filter = item['filter'] as Filter;
            return !filter.is_column_filter
        });
    }
}