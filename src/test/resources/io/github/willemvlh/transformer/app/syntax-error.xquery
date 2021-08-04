declare option saxon:output "method=json";
let $fn = function(){'abc'}
return map{'a': $fn()}