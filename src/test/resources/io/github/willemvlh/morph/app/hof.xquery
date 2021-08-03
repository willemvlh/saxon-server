declare option saxon:output "method=json";
declare option saxon:output "media-type=application/json";
let $fn := function(){'abc'}
return map{'a': $fn()}