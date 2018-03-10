function [lambda R] = load_spectra( file_name )
% LOAD_SPECTRA  load spectral response or emmision curve
% from a comma separated file. The first column must contain wavelengths in
% nm (lambda) and the remaining columns response / sensitivity / emission
% data. The data is resampled to the 360-780 range with the step of 1nm.
%
% (C) Rafal Mantiuk <mantiuk@gmail.com>

D = dlmread( file_name );
l_minmax = [360 780];
l_step = 1;

lambda = linspace( l_minmax(1), l_minmax(2), (l_minmax(2)-l_minmax(1))/l_step );

R = zeros( length(lambda), size(D,2)-1 );
for k=2:size(D,2)
    R(:,k-1) = interp1( D(:,1), D(:,k), lambda, 'pchip', 0 );
end  

end

