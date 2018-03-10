function L = gog_fw_display_model( V, gamma, L_peak, L_black, L_refl, k )
% Forward display model 
%
% L = gog_fw_display_model( V, gamma, L_peak, L_black, L_refl )
% L = gog_fw_display_model( V, gamma, L_peak, L_black, E_amb, k )
%
% V - pixel values, grayscale or color matrix
% gamma - gamma of the display (2.2 default)
% L_peak - peak luminance of the display in cd/m^2 (100 default)
% L_black - display black level (luminance emitted when displaying black
%           pixels in cd/m/^2) (0.8 default)
% L_refl - luminance reflected from the display (glare) in cd/m^2 (0 default)
% E_amb - ambient illumination in lux
% k - display reflectivity
% L - luminance emitted from a display
% 
% (c) 2012 Rafal Mantiuk


if( ~exist( 'gamma', 'var' ) || isempty( gamma ) )
    gamma = 2.2;
end
if( ~exist( 'L_peak', 'var' ) || isempty( L_peak )  )
    L_peak = 100;
end
if( ~exist( 'L_black', 'var' ) || isempty( L_black )  )
    L_black = 0.8;
end
if( ~exist( 'L_refl', 'var' ) )
    L_refl = 0;
end
if( exist( 'k', 'var' )  )
    L_refl = k*L_refl/pi;
end

if( any( V(:) )<0 || any( V(:) )>1 )
    error( 'Pixel values must be in the range 0-1)' );
end


L = (L_peak-L_black) * V.^gamma + L_black + L_refl;

end